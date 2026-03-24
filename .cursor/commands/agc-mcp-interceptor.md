---
description: AGC Phase 5 — ToolInvocationGateway + McpToolExecutor adapter; policy→guardrails; no bypass
---

Implement **Phase 5 — Tool gateway and MCP** for Agent Governance & Control.

**Flow:** Orchestrator → **`ToolInvocationGateway`** → **`GovernancePipeline`** (**`PolicyEvaluator`** → **`GuardrailEvaluator`**) → on **DENY**: **`AuditRecorder`** + structured denial (no MCP). On **ALLOW/WARN**: **`McpToolExecutor.execute`** (Spring AI adapter) → **`AuditRecorder`** (**TOOL_INVOCATION_***, **ERROR** as applicable).

**Requirements:**
- **`McpToolExecutor`** is only called from **`ToolInvocationGateway`**
- Pre-invocation: pipeline produces **`GovernanceDecision`** with **`reasonCode`** + rule ids for DENY/WARN
- Post-invocation: bounded summaries; redaction before persist
- Bypass prevention: tests (e.g. ArchUnit) or integration checks on sample module
- Explicit **ALLOW / DENY / WARN** behavior per [docs/PRODUCT_DEVELOPMENT_PLAN.md](../../docs/PRODUCT_DEVELOPMENT_PLAN.md)

**Tests:** DENY never hits MCP; ALLOW/WARN audited; tool error paths audited.
