# FTGO Role-Based Access Control (RBAC) Model

## Overview

The FTGO platform implements role-based access control (RBAC) to restrict API endpoint
access based on user roles and permissions. The system uses Spring Security's
`@PreAuthorize` annotations for method-level security, a role hierarchy for
permission inheritance, and a custom `PermissionEvaluator` for resource ownership
validation.

## Roles

| Role | Description |
|------|-------------|
| `CUSTOMER` | End users who place food orders |
| `RESTAURANT_OWNER` | Restaurant operators who manage restaurants and accept orders |
| `COURIER` | Delivery personnel who fulfill deliveries |
| `ADMIN` | Platform administrators with full access |

## Role Hierarchy

```
ROLE_ADMIN
├── ROLE_RESTAURANT_OWNER
│   └── ROLE_CUSTOMER
└── ROLE_COURIER
    └── ROLE_CUSTOMER
```

- **ADMIN** inherits all permissions of RESTAURANT_OWNER, COURIER, and CUSTOMER.
- **RESTAURANT_OWNER** inherits all permissions of CUSTOMER.
- **COURIER** inherits all permissions of CUSTOMER.

## Permission Matrix

### Consumer Service (`/api/v1/consumers`)

| Endpoint | Method | CUSTOMER | RESTAURANT_OWNER | COURIER | ADMIN |
|----------|--------|----------|-------------------|---------|-------|
| `/api/v1/consumers` | POST | ✓ | ✓ (inherits) | ✓ (inherits) | ✓ (inherits) |
| `/api/v1/consumers/{id}` | GET | Own only | Own only (inherits) | Own only (inherits) | Any |

### Order Service (`/api/v1/orders`)

| Endpoint | Method | CUSTOMER | RESTAURANT_OWNER | COURIER | ADMIN |
|----------|--------|----------|-------------------|---------|-------|
| `/api/v1/orders` | POST | ✓ | ✓ (inherits) | ✓ (inherits) | ✓ (inherits) |
| `/api/v1/orders/{id}` | GET | ✓ | ✓ | ✓ | ✓ (inherits) |
| `/api/v1/orders/{id}/cancel` | POST | ✓ | ✓ (inherits) | ✓ (inherits) | ✓ (inherits) |
| `/api/v1/orders/{id}/accept` | POST | ✗ | ✓ | ✗ | ✓ (inherits) |
| `/api/v1/orders/{id}/revise` | POST | ✓ | ✓ (inherits) | ✓ (inherits) | ✓ (inherits) |

### Restaurant Service (`/api/v1/restaurants`)

| Endpoint | Method | CUSTOMER | RESTAURANT_OWNER | COURIER | ADMIN |
|----------|--------|----------|-------------------|---------|-------|
| `/api/v1/restaurants` | POST | ✗ | ✓ | ✗ | ✓ (inherits) |
| `/api/v1/restaurants/{id}` | GET | ✓ | ✓ (inherits) | ✓ (inherits) | ✓ (inherits) |

### Courier Service (`/api/v1/couriers`)

| Endpoint | Method | CUSTOMER | RESTAURANT_OWNER | COURIER | ADMIN |
|----------|--------|----------|-------------------|---------|-------|
| `/api/v1/couriers` | POST | ✗ | ✗ | ✗ | ✓ |
| `/api/v1/couriers/{id}` | GET | ✗ | ✗ | ✓ | ✓ (inherits) |
| `/api/v1/couriers/{id}/availability` | POST | ✗ | ✗ | ✓ | ✓ (inherits) |

## Fine-Grained Permissions

Permissions follow the pattern `<context>:<action>` and are stored as JWT claims:

| Permission | Description |
|------------|-------------|
| `consumer:create` | Create a consumer profile |
| `consumer:read` | View consumer details |
| `consumer:update` | Update consumer profile |
| `consumer:delete` | Delete a consumer |
| `order:create` | Place a new order |
| `order:read` | View order details |
| `order:revise` | Revise order line items |
| `order:cancel` | Cancel an order |
| `order:accept` | Accept an order (restaurant acknowledges) |
| `restaurant:create` | Register a restaurant |
| `restaurant:read` | View restaurant details |
| `restaurant:update` | Update restaurant info |
| `restaurant:delete` | Remove a restaurant |
| `courier:create` | Register a courier |
| `courier:read` | View courier details |
| `courier:update_availability` | Update courier availability |

## JWT Claims Structure

Roles and permissions are stored in the JWT token as claims:

```json
{
  "sub": "user-123",
  "roles": ["ROLE_CUSTOMER"],
  "permissions": ["consumer:read", "order:create", "order:read"],
  "iat": 1700000000,
  "exp": 1700003600
}
```

## Resource Ownership Validation

The `FtgoPermissionEvaluator` supports resource ownership checks in `@PreAuthorize`
expressions:

```java
@PreAuthorize("hasRole('ADMIN') or hasPermission(#consumerId, 'Consumer', 'read')")
public ResponseEntity<Void> getConsumer(@PathVariable long consumerId) { ... }
```

The evaluator grants access if any of the following conditions are met:
1. The user has `ROLE_ADMIN`
2. The `targetId` matches the authenticated user's ID (resource ownership)
3. The user has the fine-grained permission `<targetType>:<permission>`

## HTTP Status Codes

| Code | Meaning |
|------|---------|
| 401 Unauthorized | No valid authentication credentials provided |
| 403 Forbidden | Authenticated but insufficient permissions for the requested resource |

Both responses are returned as structured JSON:

```json
{
  "timestamp": "2024-01-01T00:00:00Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied",
  "path": "/api/v1/couriers"
}
```

## Implementation Components

| Component | Location | Purpose |
|-----------|----------|---------|
| `FtgoRole` | `shared/ftgo-security-lib` | Enum defining the 4 platform roles |
| `FtgoPermission` | `shared/ftgo-security-lib` | Constants for fine-grained permissions |
| `RoleHierarchyConfig` | `shared/ftgo-security-lib` | Configures role hierarchy and `@EnableMethodSecurity` |
| `FtgoPermissionEvaluator` | `shared/ftgo-security-lib` | Custom permission evaluator for ownership checks |
| `FtgoSecurityAutoConfiguration` | `shared/ftgo-security-lib` | Auto-configuration importing all security components |
