# AGC — operational runbook (MVP)

Operational companion to [PRODUCT_DEVELOPMENT_PLAN.md](PRODUCT_DEVELOPMENT_PLAN.md#failure-modes--recovery). Tune alerts and dashboards to your environment.

---

## Symptoms → checks

| Symptom | Check first |
|---------|-------------|
| **503 / errors on governed tool path** | Database reachability; Flyway migration status; audit table growth / locks |
| **403 with `reasonCode` on every call** | `agc.policy.roles` and caller **roles**; guardrail `toolName` rules |
| **Startup failure: invalid auto-config** | Stale `AutoConfiguration.imports` pointing at missing classes; run `mvn clean install` |
| **Missing modules at build** | Use `mvn -pl <app> -am …` from parent or install snapshots to local repo |

---

## Audit durability (default posture)

- **Sync persist** is the intended default for compliance-style deployments.
- **Silent drop of audit** is unacceptable: log errors, expose metrics (see product plan for `agc_audit_write_failures_total` and related signals when implemented end-to-end).
- Optional **continue-on-audit-failure** (if introduced) must be **explicitly enabled** — never default in regulated postures.

---

## Scaling & storage

- Index and query pattern: **`(trace_id, sequence)`** (see storage module / migrations).
- Plan **retention** and archival before high volume; avoid storing unbounded raw payloads — use **summary + hash** per product plan.

---

## Security reminders

- **`principalId` / `roles`**: derive from authenticated identity, not from untrusted client fields in production.
- **Gateway-only MCP**: bypass is a governance failure; enforce in code review and tests.

---

## References

- [CHEAT_SHEET.md](CHEAT_SHEET.md) — build commands and curl.
- [ARCHITECTURE.md](ARCHITECTURE.md) — modules and request path.
