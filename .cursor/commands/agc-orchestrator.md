---
description: AGC Phase 6 — AgentOrchestratorService; LLM abstraction; tools only via ToolInvocationGateway
---

Implement **Phase 6 — Agent orchestrator** for Agent Governance & Control.

**Ordered flow:**
1. Receive input; generate **`traceId`** + **`correlationId`**; resolve **`principalId`** / **roles** from security context
2. **`AuditRecorder`**: request / input event
3. Call **`LlmClient`** (vendor-neutral interface); audit **LLM_INVOCATION** (bounded)
4. For each planned tool call: build **`ToolInvocationContext`** (include **`deadline`**) → invoke **`ToolInvocationGateway.invoke`** only (never MCP directly)
5. Aggregate response; audit **OUTPUT_COMPLETED**; handle LLM timeout → **SYSTEM_ERROR** audit + user-facing error

**Dependencies:** **`ToolInvocationGateway`**, **`AuditRecorder`**, **`LlmClient`** implementation supplied by app or module—no fake LLM in production path.

**Observability:** One trace per execution; span links to **`traceId`** and gateway decisions.

Reference: [docs/PRODUCT_DEVELOPMENT_PLAN.md](../../docs/PRODUCT_DEVELOPMENT_PLAN.md).
