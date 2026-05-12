# FTGO Metrics Library

Shared Micrometer/Prometheus metrics auto-configuration for FTGO platform services.

## Overview

This library provides:
- **Auto-configuration** for Micrometer metrics with Prometheus registry
- **Custom business metrics** per service domain (Order, Consumer, Restaurant, Courier)
- **Common platform metrics** for cross-cutting API tracking

## Usage

Add the dependency to your service's `build.gradle`:

```groovy
implementation project(":ftgo-metrics-lib")
```

The auto-configuration will register all metric beans automatically via Spring Boot's
`AutoConfiguration.imports` mechanism.

## Metrics Reference

### Common Platform Metrics
| Metric | Type | Description |
|--------|------|-------------|
| `ftgo.api.requests` | Counter | Total API requests (tagged by service, operation) |
| `ftgo.api.errors` | Counter | Total API errors (tagged by service, operation) |
| `ftgo.api.latency` | Timer | API request latency (tagged by service, operation) |

### Order Service Metrics
| Metric | Type | Description |
|--------|------|-------------|
| `ftgo.orders.created` | Counter | Orders created |
| `ftgo.orders.approved` | Counter | Orders approved by restaurants |
| `ftgo.orders.rejected` | Counter | Orders rejected |
| `ftgo.orders.cancelled` | Counter | Orders cancelled |
| `ftgo.orders.revised` | Counter | Orders revised |
| `ftgo.orders.delivered` | Counter | Orders delivered |
| `ftgo.orders.processing.time` | Timer | Creation to acceptance time |
| `ftgo.orders.fulfillment.time` | Timer | Creation to delivery time |

### Consumer Service Metrics
| Metric | Type | Description |
|--------|------|-------------|
| `ftgo.consumers.registered` | Counter | Consumers registered |
| `ftgo.consumers.validations.succeeded` | Counter | Successful consumer validations |
| `ftgo.consumers.validations.failed` | Counter | Failed consumer validations |

### Restaurant Service Metrics
| Metric | Type | Description |
|--------|------|-------------|
| `ftgo.restaurants.created` | Counter | Restaurants onboarded |
| `ftgo.restaurants.tickets.created` | Counter | Kitchen tickets created |
| `ftgo.restaurants.tickets.accepted` | Counter | Tickets accepted |
| `ftgo.restaurants.tickets.preparing` | Counter | Tickets in preparation |
| `ftgo.restaurants.tickets.ready` | Counter | Tickets ready for pickup |
| `ftgo.restaurants.menu.revisions` | Counter | Menu revisions |
| `ftgo.restaurants.ticket.preparation.time` | Timer | Acceptance to ready time |

### Courier Service Metrics
| Metric | Type | Description |
|--------|------|-------------|
| `ftgo.couriers.created` | Counter | Couriers created |
| `ftgo.couriers.available` | Counter | Couriers marked available |
| `ftgo.couriers.unavailable` | Counter | Couriers marked unavailable |
| `ftgo.couriers.pickups.scheduled` | Counter | Pickups scheduled |
| `ftgo.couriers.deliveries.completed` | Counter | Deliveries completed |
| `ftgo.couriers.delivery.time` | Timer | Pickup to delivery time |

## Actuator Endpoints

Services using this library should expose the following Actuator endpoints:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    tags:
      application: ${spring.application.name}
```
