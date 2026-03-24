---
description: AGC — Implement a module aligned with gateway, pipeline, and AuditRecorder contracts
---

Implement this scope for **Agent Governance & Control**:

**Module / scope:** {{MODULE_NAME}} (e.g. `governance`, `audit`, `policy`, `guardrail`, `mcp` adapter, `service` orchestrator)

**Must include:**
1. Types and packages per [docs/PRODUCT_DEVELOPMENT_PLAN.md](../../docs/PRODUCT_DEVELOPMENT_PLAN.md) (**`ToolInvocationGateway`**, **`GovernancePipeline`**, **`PolicyEvaluator`**, **`GuardrailEvaluator`**, **`AuditRecorder`**, **`McpToolExecutor`** as relevant)
2. **Governance order:** policy → guardrails → decision; **DENY** never calls **`McpToolExecutor`**
3. Unit/integration tests for decision matrix, gateway bypass prevention (where applicable), audit ordering
4. **traceId**, **`reasonCode`**, OpenTelemetry hooks on gateway and audit I/O
5. No TODOs or fake production paths

**Flow:** Orchestrator → **`ToolInvocationGateway`** → **`GovernancePipeline`** → (DENY → audit) or (ALLOW/WARN → **`McpToolExecutor`** → audit).

Skill: `.cursor/skills/agc-framework/SKILL.md`.
