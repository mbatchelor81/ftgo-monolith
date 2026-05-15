# FTGO Platform — Authorization (RBAC)

## Overview

The FTGO platform uses Role-Based Access Control (RBAC) to restrict API access
based on user roles and fine-grained permissions. Authorization is implemented
in `ftgo-security-lib` and activated automatically via Spring Boot
auto-configuration.

This builds on top of the JWT authentication layer (see
[jwt-authentication.md](jwt-authentication.md)). Roles and permissions are
carried as JWT claims and mapped to Spring Security authorities.

## Architecture

```
┌───────────┐    JWT with roles/perms    ┌───────────────────────────┐
│  Client   │───────────────────────────▶│  Microservice             │
└───────────┘                            │                           │
                                         │  1. JWT validated         │
                                         │  2. Roles → ROLE_* auths │
                                         │  3. Perms → authorities   │
                                         │  4. @PreAuthorize checked │
                                         │  5. Ownership verified    │
                                         └───────────────────────────┘
```

## Roles

| Role               | Description                               |
|--------------------|-------------------------------------------|
| `CUSTOMER`         | End user who places food orders            |
| `RESTAURANT_OWNER` | Restaurant operator who manages orders     |
| `COURIER`          | Delivery driver who picks up and delivers  |
| `ADMIN`            | Platform administrator with full access    |

Roles are stored in the JWT `realm_access.roles` claim and prefixed with
`ROLE_` when mapped to Spring Security authorities (e.g. `ROLE_CUSTOMER`).

## Permissions

Each role grants a set of fine-grained permissions. Permissions use a
`context:action` naming convention and are stored in the JWT `permissions`
claim.

### Role → Permission Matrix

| Permission               | CUSTOMER | RESTAURANT_OWNER | COURIER | ADMIN |
|--------------------------|:--------:|:-----------------:|:-------:|:-----:|
| `order:create`           |    x     |                   |         |   x   |
| `order:read`             |    x     |         x         |    x    |   x   |
| `order:cancel`           |    x     |                   |         |   x   |
| `order:revise`           |    x     |                   |         |   x   |
| `order:accept`           |          |         x         |         |   x   |
| `order:reject`           |          |         x         |         |   x   |
| `order:prepare`          |          |         x         |         |   x   |
| `order:pickup`           |          |                   |    x    |   x   |
| `order:deliver`          |          |                   |    x    |   x   |
| `consumer:read`          |    x     |                   |         |   x   |
| `consumer:create`        |          |                   |         |   x   |
| `consumer:update`        |          |                   |         |   x   |
| `restaurant:read`        |          |         x         |         |   x   |
| `restaurant:create`      |          |                   |         |   x   |
| `restaurant:update`      |          |         x         |         |   x   |
| `menu:update`            |          |         x         |         |   x   |
| `courier:read`           |          |                   |    x    |   x   |
| `courier:create`         |          |                   |         |   x   |
| `courier:update-availability` |     |                   |    x    |   x   |

## Usage

### Securing Endpoints with Roles

Use `@RequireRole` meta-annotations for role-based access:

```java
@RequireRole.Admin
@GetMapping("/api/admin/users")
public List<UserDto> listUsers() { ... }

@RequireRole.Customer
@PostMapping("/api/orders")
public OrderDto createOrder(@RequestBody CreateOrderRequest req) { ... }

@RequireRole.RestaurantOwner
@PutMapping("/api/restaurants/{id}/menu")
public MenuDto updateMenu(@PathVariable Long id, @RequestBody MenuUpdate req) { ... }

@RequireRole.Courier
@PutMapping("/api/couriers/{id}/availability")
public void updateAvailability(@PathVariable Long id, @RequestBody AvailabilityUpdate req) { ... }
```

### Securing Endpoints with Permissions

Use `@RequirePermission` meta-annotations for permission-based access:

```java
@RequirePermission.OrderCreate
@PostMapping("/api/orders")
public OrderDto createOrder(@RequestBody CreateOrderRequest req) { ... }

@RequirePermission.RestaurantUpdate
@PutMapping("/api/restaurants/{id}")
public RestaurantDto updateRestaurant(@PathVariable Long id, @RequestBody UpdateRequest req) { ... }
```

### Using @PreAuthorize Directly

For custom expressions, use `@PreAuthorize` directly:

```java
@PreAuthorize("hasAuthority('order:read')")
@GetMapping("/api/orders")
public List<OrderDto> listOrders() { ... }

@PreAuthorize("hasRole('ADMIN') or hasAuthority('order:cancel')")
@DeleteMapping("/api/orders/{id}")
public void cancelOrder(@PathVariable Long id) { ... }
```

### Resource Ownership Checks

Use `hasPermission()` with a target type to combine authority + ownership checks:

```java
@PreAuthorize("hasPermission(#orderId, 'Order', 'order:read')")
@GetMapping("/api/orders/{orderId}")
public OrderDto getOrder(@PathVariable Long orderId) { ... }
```

This verifies:
1. The user holds the `order:read` permission
2. The user owns the resource (via `ResourceOwnershipChecker`), OR the user is an ADMIN

### Implementing ResourceOwnershipChecker

Each service registers ownership checkers for its domain entities:

```java
@Component
public class OrderOwnershipChecker implements ResourceOwnershipChecker {

    private final OrderRepository repository;

    public OrderOwnershipChecker(OrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public String getTargetType() {
        return "Order";
    }

    @Override
    public boolean isOwner(String userId, Serializable resourceId) {
        return repository.findById((Long) resourceId)
                .map(order -> order.getConsumerId().toString().equals(userId))
                .orElse(false);
    }
}
```

## JWT Claims Example

```json
{
  "sub": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "preferred_username": "consumer1",
  "email": "consumer1@ftgo.example",
  "realm_access": {
    "roles": ["CUSTOMER"]
  },
  "permissions": ["order:create", "order:read", "order:cancel", "order:revise", "consumer:read"]
}
```

## Configuration

Authorization is enabled by default when `ftgo.security.enabled=true`.
No additional configuration is required beyond the JWT configuration
documented in [jwt-authentication.md](jwt-authentication.md).

```yaml
ftgo:
  security:
    enabled: true
    jwt:
      enabled: true
      issuer-uri: http://localhost:8180/realms/ftgo
      jwk-set-uri: http://localhost:8180/realms/ftgo/protocol/openid-connect/certs
      roles-claim-name: realm_access.roles
      permissions-claim-name: permissions
```

## ADMIN Bypass

Users with the `ROLE_ADMIN` authority bypass all resource ownership checks
in `hasPermission()` evaluations. They still need the required permission
authority. This allows administrators to manage any resource regardless of
ownership.

## Module Structure

```
libs/ftgo-security-lib/
└── src/main/java/com/ftgo/security/
    └── authorization/
        ├── FtgoRole.java                        # Role enum with permission sets
        ├── FtgoPermission.java                  # Permission enum with authorities
        ├── FtgoPermissionEvaluator.java          # Custom PermissionEvaluator
        ├── ResourceOwnershipChecker.java         # Ownership strategy interface
        ├── FtgoAuthorizationAutoConfiguration.java # Auto-config with @EnableMethodSecurity
        ├── RequireRole.java                     # Role-based meta-annotations
        └── RequirePermission.java               # Permission-based meta-annotations
```

## Per-Service Security Configuration

Each microservice defines endpoint security rules matching its bounded context:

| Service             | Typical Rules                                          |
|---------------------|--------------------------------------------------------|
| Consumer Service    | `CUSTOMER` for own profile, `ADMIN` for all consumers  |
| Order Service       | `CUSTOMER` create/cancel, `RESTAURANT_OWNER` accept/prepare, `COURIER` pickup/deliver |
| Restaurant Service  | `RESTAURANT_OWNER` manage own restaurants, `ADMIN` create |
| Courier Service     | `COURIER` manage own availability, `ADMIN` create       |

## Testing

Test authorization with Spring Security test support:

```java
@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerAuthzTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void customerCanCreateOrder() throws Exception {
        String token = createJwt("customer-1", List.of("CUSTOMER"), List.of("order:create"));

        mockMvc.perform(post("/api/orders")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isCreated());
    }

    @Test
    void courierCannotCreateOrder() throws Exception {
        String token = createJwt("courier-1", List.of("COURIER"), List.of("order:read"));

        mockMvc.perform(post("/api/orders")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isForbidden());
    }
}
```
