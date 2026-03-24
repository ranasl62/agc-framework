---
description: AGC Phase 9 — REST execute + audit; Problem Details; denial schema with reasonCode
---

Implement **Phase 9 — API layer** for Agent Governance & Control.

**Endpoints (baseline):**
- **`POST /agent/execute`** — body includes message + auth context for policy; returns response + **`traceId`**
- **`GET /audit/{traceId}`** — returns **`AuditEvent`** timeline (paged if large), ordered by **`sequence`**
- Policy/guardrail management endpoints as designed

**Requirements:**
- Validation on bodies; **RFC 7807 Problem Details** (or one consistent JSON error shape)
- **Denial responses** include **`reasonCode`**, **matched rule ids**; no stack traces or secrets
- Request / payload **size limits**; redact tool payloads in responses per config
- Controllers in `com.framework.agent.controller`; delegate to orchestrator/gateway—**no** duplicate governance logic

Reference: [docs/PRODUCT_DEVELOPMENT_PLAN.md](../../docs/PRODUCT_DEVELOPMENT_PLAN.md).
