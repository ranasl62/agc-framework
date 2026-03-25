# Agent Governance & Control (AGC)

[![CI](https://github.com/ranasl62/agc-framework/actions/workflows/ci.yml/badge.svg)](https://github.com/ranasl62/agc-framework/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.framework.agent/agc-spring-boot-starter?label=Maven%20Central)](https://search.maven.org/search?q=g:com.framework.agent)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

**AGC is a Spring Boot library that enforces governance for LLM tool execution**—policy, guardrails, registry checks, and an append-only audit trail—so teams can ship agents without silent bypasses or missing compliance signals.

**For:** Spring Boot teams building agents or copilots with tool access, especially where auditability and least-privilege matter.

| | |
|--|--|
| **Official name** | **Agent Governance & Control** (**AGC**) |
| **Maven `groupId`** | `com.framework.agent` |
| **Main artifact** | `agc-spring-boot-starter` |
| **Library guide (publish, keywords, why adopt)** | **[docs/LIBRARY.md](docs/LIBRARY.md)** |

---

## Find this project

**Maven Central:** search [`g:com.framework.agent`](https://search.maven.org/search?q=g:com.framework.agent) or artifact [`agc-spring-boot-starter`](https://search.maven.org/search?q=a:agc-spring-boot-starter).

**Typical search keywords:** `AGC Spring Boot`, `LLM tool governance Java`, `agent tool audit`, `Spring Boot AI policy`, `ToolInvocationGateway`, `com.framework.agent`, `governed tool execution`, `MCP Java governance` (conceptual; AGC is gateway-centric).

**GitHub topics (recommended for the repo):** `spring-boot`, `java`, `llm`, `ai-agents`, `tool-calling`, `governance`, `audit`, `policy`, `authorization`, `mcp`, `enterprise-ai`, `compliance`.

---

## What is AGC?

AGC sits on the **ToolInvocationGateway**: every governed tool call flows through **ToolRegistry → policy → guardrails → execution**, then **audit**. Optional REST (`agc-api`) exposes execute and audit-query endpoints. Deep detail: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

---

## Why AGC?

Agents that call tools without a single choke point tend to leak privilege: wrong roles, unlogged actions, and “helpful” shortcuts that skip checks. AGC makes **one path** the norm—gateway-only execution, explainable **DENY** reasons, and storage you can review by `traceId`.

---

## When should you use AGC?

Use AGC if you:

- Build AI agents or assistants that invoke backend tools
- Need an **audit trail** for security or compliance conversations
- Want to **block tool misuse** (wrong role, unknown tool, guardrail rule) without ad-hoc `if` chains
- Prefer **fail-closed** behavior when governance or audit cannot complete safely

---

## Key capabilities

- **Gateway-only execution** — `ToolInvocationGateway` is the supported path; internal executor use is blocked at runtime and by ArchUnit.
- **Ordered governance pipeline** — registry first, then policy and guardrails, then MCP adapter execution.
- **Explicit decisions** — `ALLOW` / `DENY` / `WARN` with stable `reasonCode` values for logs and APIs.
- **Audit modes** — `STRICT` (default), `ASYNC`, `BEST_EFFORT`; governed-path behavior is documented in [docs/FAILURE_MODES.md](docs/FAILURE_MODES.md).
- **Production identity** — with `agc.governance.mode: PRODUCTION`, REST uses Spring Security for principal/roles, not the request body alone.

---

## Execution flow

```
Client / orchestrator
    → ToolInvocationGateway
    → ToolRegistry (optional allowlist)
    → Policy
    → Guardrails
    → Internal tool adapter (MCP)
    → Audit trail (per agc.audit.mode)
```

---

## Example denial (REST)

`POST /agent/execute` returns **403** with a Problem Details body (fields vary slightly by case). Typical tool denial:

```json
{
  "type": "about:blank",
  "title": "Tool invocation denied",
  "status": 403,
  "detail": "POLICY_TOOL_FORBIDDEN",
  "decision": "DENY",
  "reasonCode": "POLICY_TOOL_FORBIDDEN",
  "matchedRuleIds": ["policy"],
  "traceId": "t-demo"
}
```

Tool name and event sequence appear on **`GET /audit/{traceId}`** (ordered by `sequenceNum`).

---

## Quickstart

**1.** Add the starter (version **1.0.0** matches the current release line on Maven Central after you publish):

```xml
<dependency>
  <groupId>com.framework.agent</groupId>
  <artifactId>agc-spring-boot-starter</artifactId>
  <version>1.0.0</version>
</dependency>
```

Optional REST controllers:

```xml
<dependency>
  <groupId>com.framework.agent</groupId>
  <artifactId>agc-api</artifactId>
  <version>1.0.0</version>
</dependency>
```

**2.** Minimal `application.yml` (H2 in-memory is fine for a first run; match your DB in production):

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:demo;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa.hibernate.ddl-auto: validate
  flyway.enabled: true

agc:
  enabled: true
  governance:
    mode: DEVELOPMENT
  tools:
    allowed: [search]
  policy:
    roles:
      user: [search]
```

**3.** Call execute (demo uses `[[tool:search]]` in the message for the stub LLM):

```bash
curl -s -X POST http://localhost:8080/agent/execute \
  -H 'Content-Type: application/json' \
  -d '{"traceId":"t-1","correlationId":"c-1","principalId":"u1","roles":["user"],"message":"Use [[tool:search]]"}'
```

**Expected:** **200** with JSON including `success: true` and an `outcomeSummary` produced by your `McpToolExecutor` (the runnable demo returns summaries like `demo-backend.search …`).  
If the tool is not allowed: **403** with `decision` / `reasonCode` as above.

More copy-paste flow: [docs/QUICKSTART.md](docs/QUICKSTART.md).

---

## Modules

| Module | Role |
|--------|------|
| **agc-core** | Domain + SPIs (`com.framework.agent.core`), no Spring |
| **agc-storage** / **agc-audit** | JPA, Flyway, `JpaAuditRecorder` |
| **agc-policy** / **agc-guardrail** | Role→tools config, typed rules |
| **agc-mcp** | `ToolInvocationGateway`, governance pipeline; executor in **`mcp.internal`** |
| **agc-orchestrator** | `AgentOrchestrator` |
| **agc-api** | Optional REST: `POST /agent/execute`, `GET /audit/{traceId}` |
| **agc-spring-boot-autoconfigure** | All `@AutoConfiguration` classes |
| **agc-spring-boot-starter** | Depends only on autoconfigure |
| **agc-architecture-tests** | ArchUnit (gateway boundary) |
| **agc-demo-app** | Runnable sample (starter + API + H2) |

---

## Demo

```bash
mvn -pl agc-demo-app -am spring-boot:run
```

Open [http://localhost:8080/](http://localhost:8080/) — **Quick try** (allow / policy deny / guardrail deny), **Audit trail** (`sequenceNum` order), **`GET /demo/backend/*`** (synthetic microservice JSON), and **`POST /demo/mcp`** (JSON-RPC `tools/list` + `tools/call` through the same `ToolInvocationGateway`).

---

## Installation

**Release (Maven Central):**

```xml
<dependency>
  <groupId>com.framework.agent</groupId>
  <artifactId>agc-spring-boot-starter</artifactId>
  <version>1.0.0</version>
</dependency>
```

**From source:**

```bash
mvn clean install
```

For local development off unreleased commits, use `mvn install` and a `*-SNAPSHOT` version (see [docs/RELEASING.md](docs/RELEASING.md)).

---

## Design principles

- **Fail closed** — governance and `STRICT` audit errors stop unsafe execution.
- **No bypass** — tool execution goes through `ToolInvocationGateway`, not around it.
- **Explainable decisions** — every denial carries a `reasonCode` (and usually `matchedRuleIds`).
- **Append-only audit** — events are recorded for review by `traceId` (ordering by `sequenceNum`).

---

## Docs

| Doc | Purpose |
|-----|---------|
| [docs/LIBRARY.md](docs/LIBRARY.md) | **Canonical library guide:** naming, Maven coordinates, why adopt, importance, discoverability & search terms, publishing |
| [docs/USAGE.md](docs/USAGE.md) | **How to use each feature:** call order, `ToolInvocationContext`, audit storage, passing data, exceptions & HTTP mapping |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Modules, auto-config order, config keys, HTTP examples |
| [docs/GOVERNANCE.md](docs/GOVERNANCE.md) | Decisions, reason codes, policy model |
| [docs/FAILURE_MODES.md](docs/FAILURE_MODES.md) | Audit modes, DB down, tool errors |
| [docs/QUICKSTART.md](docs/QUICKSTART.md) | Minimal setup + one working path |
| [docs/RELEASING.md](docs/RELEASING.md) | **Maven Central:** prerequisites, GPG, `deploy -Prelease`, post-release version bump |

---

## License

Apache License 2.0 — [LICENSE](LICENSE).

[CONTRIBUTING.md](CONTRIBUTING.md) · [SECURITY.md](SECURITY.md) · **[Publishing → docs/RELEASING.md](docs/RELEASING.md)**
