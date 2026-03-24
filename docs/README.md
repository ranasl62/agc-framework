# Documentation

| Document | Description |
|----------|-------------|
| [PRODUCT_DEVELOPMENT_PLAN.md](PRODUCT_DEVELOPMENT_PLAN.md) | Full product plan: phases 0–11, **architectural invariants** (`ToolInvocationGateway`, `GovernancePipeline`), **ALLOW/DENY/WARN** semantics, **Java-style core contracts**, **AuditEvent** stream, **failure modes**, performance/security notes, strict **MVP** scope, dependency graph |

**Code layout:** see repository [README.md](../README.md) — `agc-core` is pure Java (`com.framework.agent.core`); **`agc-spring-boot-starter`** aggregates **`agc-spring-boot-autoconfigure`** (all `@AutoConfiguration`); **`agc-api`** is the optional REST layer.

Implementation prompts and conventions for Cursor live under [`.cursor/`](../.cursor/).
