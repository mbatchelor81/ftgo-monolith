# FTGO Microservices

This directory contains the target microservice structure for the FTGO platform
migration. Each subdirectory represents an independent, deployable service
aligned to a bounded context.

## Services

| Service                   | Bounded Context | Package Root                    |
|---------------------------|-----------------|---------------------------------|
| `ftgo-consumer-service`   | Consumer        | `com.ftgo.consumerservice`      |
| `ftgo-order-service`      | Order           | `com.ftgo.orderservice`         |
| `ftgo-restaurant-service` | Restaurant      | `com.ftgo.restaurantservice`    |
| `ftgo-courier-service`    | Courier         | `com.ftgo.courierservice`       |

## Shared Libraries

| Library            | Purpose                                    |
|--------------------|--------------------------------------------|
| `ftgo-common`      | Cross-cutting value objects and utilities   |
| `ftgo-common-jpa`  | Shared JPA base entities and configuration |

## Creating a New Service

Use the `ftgo-service-template/` directory as a starting point. See its
[README](ftgo-service-template/README.md) for step-by-step instructions.

## Conventions

All naming conventions are documented in
[ADR-0001](../docs/adr/0001-microservices-repository-structure.md).
