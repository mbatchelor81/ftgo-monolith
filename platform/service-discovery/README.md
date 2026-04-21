# Service Discovery

Service registry used by FTGO microservices to locate each other without
hard-coded hostnames. Likely implemented as Kubernetes DNS + `Service`
resources plus a thin Spring Cloud LoadBalancer configuration library.

**Scaffold only.** Implementation is tracked by EM-44.
