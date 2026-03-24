---
description: AGC Phase 2 — AuditEvent stream, AuditRecorder, bounded payloads, append-only, failure metrics
---

Implement **Phase 2 — Audit trail** for Agent Governance & Control.

**Model:** **`AuditEvent`** append-only stream (not a single wide “mega-row” for all step types). Fields align with plan: `traceId`, monotonic **`sequence`** per trace, `timestamp`, **`AuditEventType`**, optional embedded **`GovernanceDecision`**, `toolName`, bounded **`payloadSummary`**, optional **`payloadHash`**.

**Deliverables:**
- JPA (or chosen store) + repository; query ordered by **`(traceId, sequence)`**
- **`AuditRecorder`** implementation; **redaction SPI**; enforce **max payload** bytes
- Metric **`agc_audit_write_failures_total`**; default **fail-closed** on governed path when sync persist fails (or explicit opt-in dev flag per plan)
- No update/delete APIs for audit data

**Tests:** `@DataJpaTest` / Testcontainers as used in project; ordering and bound enforcement.

Canonical reference: [docs/PRODUCT_DEVELOPMENT_PLAN.md](../../docs/PRODUCT_DEVELOPMENT_PLAN.md).
