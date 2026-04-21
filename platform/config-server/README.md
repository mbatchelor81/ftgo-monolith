# Config Server

Centralized configuration for FTGO microservices. Lets every service bootstrap
its `spring.config.import=configserver:` without redeploying when a value
changes.

**Scaffold only.** Design to be finalized alongside EM-38.
