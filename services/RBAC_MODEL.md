# FTGO Role-Based Access Control (RBAC) Model

This document defines the authorization framework for the FTGO microservices platform.

## Roles

| Role | Description |
|------|-------------|
| `CUSTOMER` | End-user who places food orders |
| `RESTAURANT_OWNER` | Restaurant owner who manages restaurants and processes orders |
| `COURIER` | Delivery courier who picks up and delivers orders |
| `ADMIN` | Platform administrator with full access to all resources |

## Role Hierarchy

```
ADMIN
├── RESTAURANT_OWNER
│   └── CUSTOMER
└── COURIER
```

- **ADMIN** inherits all permissions from RESTAURANT_OWNER, COURIER, and CUSTOMER.
- **RESTAURANT_OWNER** inherits all permissions from CUSTOMER.
- **COURIER** does not inherit from any other role.
- **CUSTOMER** is the base role with minimal permissions.

## Permission Matrix

Permissions follow the pattern `{domain}:{action}` or `{domain}:{action}:{scope}`.
The `:own` suffix indicates ownership-scoped permissions requiring the user to own the resource.

### Consumer Service

| Permission | CUSTOMER | RESTAURANT_OWNER | COURIER | ADMIN |
|------------|----------|-------------------|---------|-------|
| `consumer:create` | | | | X |
| `consumer:read` | | | | X |
| `consumer:read:own` | X | | | X |

### Order Service

| Permission | CUSTOMER | RESTAURANT_OWNER | COURIER | ADMIN |
|------------|----------|-------------------|---------|-------|
| `order:create` | X | | | X |
| `order:read` | | X | | X |
| `order:read:own` | X | | X | X |
| `order:cancel` | | | | X |
| `order:cancel:own` | X | | | X |
| `order:revise` | | | | X |
| `order:accept` | | X | | X |
| `order:status:update` | | X | | X |

### Restaurant Service

| Permission | CUSTOMER | RESTAURANT_OWNER | COURIER | ADMIN |
|------------|----------|-------------------|---------|-------|
| `restaurant:create` | | X | | X |
| `restaurant:read` | X | X | | X |
| `restaurant:update` | | | | X |
| `restaurant:update:own` | | X | | X |
| `restaurant:delete` | | X | | X |

### Courier Service

| Permission | CUSTOMER | RESTAURANT_OWNER | COURIER | ADMIN |
|------------|----------|-------------------|---------|-------|
| `courier:create` | | | | X |
| `courier:read` | | | | X |
| `courier:read:own` | X | | X | X |
| `courier:update:availability` | | | | X |
| `courier:update:availability:own` | | | X | X |
| `courier:delivery:update` | | | X | X |

## Implementation

### Architecture

The RBAC framework is implemented in `ftgo-security-lib` and auto-configured for all services:

```
ftgo-security-lib/
└── com.ftgo.security.authorization/
    ├── FtgoRole.java                    # Role enum (CUSTOMER, RESTAURANT_OWNER, COURIER, ADMIN)
    ├── FtgoPermission.java              # Permission constants
    ├── RolePermissionMapping.java       # Role-to-permission mapping
    ├── FtgoPermissionEvaluator.java     # Custom Spring Security PermissionEvaluator
    ├── ResourceOwnershipResolver.java   # Strategy interface for ownership checks
    ├── MethodSecurityConfiguration.java # @EnableMethodSecurity + expression handler
    └── RoleHierarchyConfiguration.java  # Spring RoleHierarchy bean
```

### JWT Claims

Roles and permissions are stored in JWT token claims:

```json
{
  "sub": "john.doe",
  "userId": "user-123",
  "roles": ["CUSTOMER"],
  "permissions": ["order:create", "order:read:own"],
  "type": "access"
}
```

### Method-Level Security

Use `@PreAuthorize` annotations on service methods or controller endpoints:

```java
// Role-based access
@PreAuthorize("hasRole('ADMIN')")
public Consumer createConsumer(CreateConsumerRequest request) { ... }

// Role with ownership check
@PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') "
    + "and @orderOwnershipResolver.isOwner("
    + "T(com.ftgo.security.util.SecurityUtils).getCurrentUserId().orElse(''), "
    + "#orderId, 'Order'))")
public Order getOrder(Long orderId) { ... }

// Multiple roles
@PreAuthorize("hasRole('ADMIN') or hasRole('RESTAURANT_OWNER')")
public void acceptOrder(Long orderId) { ... }

// Permission-based (via PermissionEvaluator)
@PreAuthorize("hasPermission(#id, 'Order', 'order:read:own')")
public Order findById(Long id) { ... }
```

### Resource Ownership

Each service implements `ResourceOwnershipResolver` for its domain entities:

```java
@Component
public class OrderOwnershipResolver implements ResourceOwnershipResolver {

    private final OrderRepository orderRepository;

    @Override
    public boolean supports(String resourceType) {
        return "Order".equals(resourceType);
    }

    @Override
    public boolean isOwner(String userId, Serializable resourceId, String resourceType) {
        return orderRepository.findById((Long) resourceId)
            .map(order -> userId.equals(String.valueOf(order.getConsumerId())))
            .orElse(false);
    }
}
```

## HTTP Response Codes

| Scenario | Status Code |
|----------|-------------|
| Missing or invalid JWT | `401 Unauthorized` |
| Valid JWT but insufficient permissions | `403 Forbidden` |
| Authenticated and authorized | `200 OK` (or appropriate success code) |

## Security Configuration

The RBAC framework is automatically activated via Spring Boot auto-configuration when
a service includes `ftgo-security-lib` as a dependency. No additional configuration is
needed in individual services beyond implementing `ResourceOwnershipResolver` for
service-specific entities.
