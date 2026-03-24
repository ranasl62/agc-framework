---
name: agc-framework
description: Agent Governance & Control — multi-module Spring layout; pure-Java agc-core; agc-spring-boot-autoconfigure for all @AutoConfiguration; starter is aggregator only; McpToolExecutor in mcp.internal; optional agc-api REST. Use when changing governance, audit, policy, guardrails, gateway, or module boundaries.
---

# Agent Governance & Control (AGC)

**Canonical spec:** [docs/PRODUCT_DEVELOPMENT_PLAN.md](../../../docs/PRODUCT_DEVELOPMENT_PLAN.md)

**Repo layout:** [README.md](../../../README.md) · **implementation map:** [docs/ARCHITECTURE.md](../../../docs/ARCHITECTURE.md) · **quick ref:** [docs/CHEAT_SHEET.md](../../../docs/CHEAT_SHEET.md)

## Module map

| Module | Contents |
|--------|-----------|
| **agc-core** | **Pure Java only** — `GovernanceDecision`, `ToolInvocationContext`, `AuditEvent`, `PolicyEvaluator`, `GuardrailEvaluator`, `ToolInvocationGateway`, etc. (`com.framework.agent.core`) |
| **agc-storage** | JPA + Flyway (`com.framework.agent.storage`) |
| **agc-audit** | `JpaAuditRecorder`, `agc.audit.*` |
| **agc-policy** | `RoleToolPolicyEvaluator`, `agc.policy.roles` |
| **agc-guardrail** | `ListGuardrailEvaluator`, `agc.guardrails.rules` |
| **agc-mcp** | `DefaultGovernancePipeline`, `DefaultToolInvocationGateway`; executor SPI in `com.framework.agent.mcp.internal` |
| **agc-spring-boot-autoconfigure** | All `@AutoConfiguration` classes (`Agc*AutoConfiguration`) |
| **agc-orchestrator** | `AgentOrchestrator`, stub LLM `agc.llm.*` |
| **agc-observability** | Placeholder package; Micrometer tags applied from autoconfigure when `MeterRegistry` exists |
| **agc-api** | REST controllers (`com.framework.agent.api.web`) |
| **agc-spring-boot-starter** | **Aggregator only** → `agc-spring-boot-autoconfigure` (transitive feature JARs; add `agc-api` for REST) |
| **agc-demo-app** | Sample: starter + `agc-api` + H2 |

## Invariants

- All governed tools: **`ToolInvocationGateway`** only.
- Pipeline order: **policy → guardrails**; **DENY** skips MCP.
- Audit: append-only **`AuditEvent`** stream; bounded payloads.

## Commands

`.cursor/commands/agc-*.md` — align file paths with modules above.
