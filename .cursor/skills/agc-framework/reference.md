# AGC — Phase reference (concise)

Full detail: [docs/PRODUCT_DEVELOPMENT_PLAN.md](../../../docs/PRODUCT_DEVELOPMENT_PLAN.md).

- [docs/ARCHITECTURE.md](../../../docs/ARCHITECTURE.md) — modules, packages, auto-config
- [docs/CHEAT_SHEET.md](../../../docs/CHEAT_SHEET.md) — build, config, curl
- [docs/RUNBOOK.md](../../../docs/RUNBOOK.md) — ops checklist

## Code modules (implementation map)

| Concept | Module(s) |
|---------|-----------|
| Domain / interfaces | **agc-core** (`com.framework.agent.core`) |
| DB / Flyway / sequence | **agc-storage** |
| AuditRecorder impl | **agc-audit** |
| Policy | **agc-policy** |
| Guardrails | **agc-guardrail** |
| Gateway + pipeline | **agc-mcp** |
| Orchestrator + stub LLM | **agc-orchestrator** |
| Metrics / OTel hooks | **agc-observability** |
| REST | **agc-api** (`com.framework.agent.api.web`) |
| Spring Boot wiring | **agc-spring-boot-autoconfigure** (`com.framework.agent.autoconfigure`) |
| User dependency | **agc-spring-boot-starter** (aggregator → autoconfigure) |
| ArchUnit | **agc-architecture-tests** |
| Sample | **agc-demo-app** |

## Phases 0–11

Same as product plan; map work to modules above (e.g. Phase 2 → `agc-storage` + `agc-audit`, Phase 5 → `agc-mcp`).

## MVP slice

Starter + storage + audit + policy + one guardrail + MCP gateway + orchestrator + optional `agc-api` + demo.
