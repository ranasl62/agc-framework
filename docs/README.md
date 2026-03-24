# Documentation

| Document | Description |
|----------|-------------|
| [ARCHITECTURE.md](ARCHITECTURE.md) | Module graph, packages, auto-config order, governance runtime checks |
| [CHEAT_SHEET.md](CHEAT_SHEET.md) | Build, config keys, HTTP smoke |
| [RUNBOOK.md](RUNBOOK.md) | Operational checks |

**Code layout:** see repository [README.md](../README.md) — `agc-core` is pure Java (`com.framework.agent.core`); **`agc-spring-boot-starter`** aggregates **`agc-spring-boot-autoconfigure`**; **`agc-api`** is the optional REST layer.
