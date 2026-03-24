# Agent Governance & Control (AGC)

Spring Boot library for **governed** agent tool calls: **policy → guardrails → gateway**, **append-only audit**, and optional REST.  
**Technical detail:** [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)

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

Then open [http://localhost:8080/](http://localhost:8080/) for the scenario UI, or call `POST /agent/execute` / `GET /audit/{traceId}` as in [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

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
