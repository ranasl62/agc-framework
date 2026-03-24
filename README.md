# Agent Governance & Control (AGC)

Spring Boot library for **governed** agent tool calls: **gateway → tool registry → policy → guardrails → execution**, **append-only audit**, and optional REST.

**Repository:** [github.com/ranasl62/agc-framework](https://github.com/ranasl62/agc-framework) · **Architecture:** [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)

**Production-oriented controls (summary):** all tool calls go through `ToolInvocationGateway`; unknown tools are denied (`TOOL_NOT_REGISTERED`); `agc.enabled` is a global kill switch; `agc.audit.mode` is `STRICT` | `ASYNC` | `BEST_EFFORT`; `agc.governance.mode` includes `PRODUCTION` (REST identity from Spring Security, not JSON alone).

## Modules (summary)

| Module | Role |
|--------|------|
| **agc-core** | Domain + SPIs (`com.framework.agent.core`), no Spring |
| **agc-storage** / **agc-audit** | JPA, Flyway, `JpaAuditRecorder` |
| **agc-policy** / **agc-guardrail** | Role→tools config, typed rules |
| **agc-mcp** | `ToolInvocationGateway`, pipeline; executor SPI in **`mcp.internal`** |
| **agc-orchestrator** | `AgentOrchestrator`, stub LLM |
| **agc-api** | REST: `POST /agent/execute`, `GET /audit/{traceId}` (optional) |
| **agc-spring-boot-autoconfigure** | All `@AutoConfiguration` (ordered chain) |
| **agc-spring-boot-starter** | Depends only on autoconfigure |
| **agc-architecture-tests** | ArchUnit (gateway boundary) |
| **agc-demo-app** | Runnable sample (starter + API + H2) |

## Build

```bash
mvn clean verify
```

CI runs `mvn verify` on push/PR (`.github/workflows/ci.yml`).

## Run the demo

```bash
mvn -pl agc-demo-app -am spring-boot:run
```

Then open [http://localhost:8080/](http://localhost:8080/) for the grouped scenario UI (registry, policy, guardrails, success paths), or call `POST /agent/execute` / `GET /audit/{traceId}` as in [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md). **`POST /demo/run`** runs named scenarios from [`DemoScenario`](agc-demo-app/src/main/java/com/framework/agent/demo/DemoScenario.java).

## Use as a dependency

```xml
<dependency>
  <groupId>com.framework.agent</groupId>
  <artifactId>agc-spring-boot-starter</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

Add **`agc-api`** for the REST controllers. Build from source with `mvn install` until artifacts are published to a registry.

## License

Apache License 2.0 — [LICENSE](LICENSE).

[CONTRIBUTING.md](CONTRIBUTING.md) · [SECURITY.md](SECURITY.md)
