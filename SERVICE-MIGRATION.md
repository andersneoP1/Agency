# Service migration

Seven business modules are now independently executable Spring Boot services.
See [CLOUD-ARCHITECTURE.md](CLOUD-ARCHITECTURE.md) for the current architecture,
Docker Compose startup, API documentation, monitoring and verification commands.

Shared persistence and the optional legacy web application remain migration
limitations; they are explicitly documented there.
