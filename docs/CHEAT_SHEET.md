# AGC — cheat sheet

Quick reference for developers and operators. Structure: [ARCHITECTURE.md](ARCHITECTURE.md).

---

## Build & run

| Goal | Command |
|------|---------|
| Full build + tests | `mvn clean verify` |
| Run demo (from repo root) | `mvn -pl agc-demo-app -am spring-boot:run` |
| Install to `~/.m2`, then demo only | `mvn clean install` then `mvn -pl agc-demo-app spring-boot:run` |

`-am` (*also-make*) builds required sibling modules in the same reactor. Without it, unresolved `0.1.0-SNAPSHOT` artifacts cause failures.

---

## Maven coordinates

```xml
<dependency>
  <groupId>com.framework.agent</groupId>
  <artifactId>agc-spring-boot-starter</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

Add **`agc-api`** for `POST /agent/execute` and `GET /audit/{traceId}`.

---

## Configuration keys (demo-aligned)

| Prefix / key | Purpose |
|--------------|---------|
| `agc.policy.roles` | Map role name → list of allowed tool names; `"*"` = all tools (see demo) |
| `agc.guardrails.rules` | List of rules: `id`, `toolName`, `action` (`DENY` / `WARN`) |
| `agc.llm.planned-tool-name` | Stub orchestrator: which tool name to plan (demo) |
| `agc.audit.max-payload-chars` | Bound persisted audit payload text |
| `agc.audit.strict-secondary-audit` | Default `true`: if persisting `SYSTEM_ERROR` after a tool failure fails, throws `GovernedPathAuditException` (fail-closed) |

Example (excerpt): see `agc-demo-app/src/main/resources/application.yml`.

---

## HTTP smoke (demo on port 8080)

**Execute (minimal JSON):**

```bash
curl -s -X POST http://localhost:8080/agent/execute \
  -H 'Content-Type: application/json' \
  -d '{"principalId":"u1","roles":["user"],"message":"hello"}' | jq .
```

**Audit trail (use `traceId` from response):**

```bash
curl -s "http://localhost:8080/audit/<traceId>" | jq .
```

**Forbidden (403)** responses use **RFC 7807** `ProblemDetail` with `reasonCode`, `matchedRuleIds`, `traceId` when the gateway denies invocation.

### Demo application (`agc-demo-app`)

| What | URL / note |
|------|------------|
| Scenario UI | `GET /` (static `index.html`) |
| Scenario catalog | `GET /demo/scenarios` |
| Run scenario | `POST /demo/run` with `{"scenario":"allow_search"}` (ids match `DemoScenario`) |
| Health | `GET /actuator/health` |
| H2 console | `GET /h2-console` (demo only; JDBC `jdbc:h2:mem:agc`, user `sa`) |

Planned tool for the stub LLM: **`[[tool:name]]`** in the user message, or per-scenario override inside `/demo/run`.

---

## Invariants (one line each)

- Governed tools: **only** via `ToolInvocationGateway`.
- Order: **policy → guardrails**; **DENY** → no MCP/tool executor call.
- Audit: **append-only** stream; **traceId + sequence** ordering.

---

## Operations pointer

Degraded behavior: [RUNBOOK.md](RUNBOOK.md).
