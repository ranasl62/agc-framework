# Agent Governance & Control (AGC)

Spring Boot framework for governed agent tool execution, append-only audit, and policy/guardrails.

## Module layout

| Module | Role |
|--------|------|
| **agc-core** | Pure Java domain + interfaces (`com.framework.agent.core`) — **no Spring** |
| **agc-storage** | JPA entities, repositories, Flyway migrations, trace sequence |
| **agc-audit** | `JpaAuditRecorder`, payload redaction, `agc.audit.*` properties |
| **agc-policy** | Role → allowed tools (`agc.policy.roles`) |
| **agc-guardrail** | Typed rules (`agc.guardrails.rules`) |
| **agc-mcp** | `DefaultGovernancePipeline`, `ToolInvocationGateway`, `McpToolExecutor` stub |
| **agc-orchestrator** | `AgentOrchestrator`, stub LLM (`agc.llm.*`) |
| **agc-observability** | Micrometer tags (extend for OpenTelemetry) |
| **agc-api** | REST: `POST /agent/execute`, `GET /audit/{traceId}` |
| **agc-spring-boot-starter** | **Single dependency for users** (transitive modules above; **no** REST unless you add `agc-api`) |
| **agc-demo-app** | Runnable sample: starter + `agc-api` + H2 |

## Build

```bash
mvn clean verify
```

On GitHub, **CI** runs the same (`mvn -B verify`) on pushes and pull requests to `main` or `master` (see `.github/workflows/ci.yml`).

## Run demo

From the **repository root**, build dependent modules in the same reactor and start the app:

```bash
mvn -pl agc-demo-app -am spring-boot:run
```

`-am` (*also-make*) compiles/installs `agc-spring-boot-starter`, `agc-api`, and their transitive AGC modules into the local reactor. Using **only** `-pl agc-demo-app` skips those siblings, so Maven looks in `~/.m2` for `0.1.0-SNAPSHOT` jars and fails if you have not run `mvn install` from root first.

**Alternative:** install once, then run:

```bash
mvn clean install
mvn -pl agc-demo-app spring-boot:run
```

## User dependency (headless, no REST)

```xml
<dependency>
  <groupId>com.framework.agent</groupId>
  <artifactId>agc-spring-boot-starter</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

Add **agc-api** for bundled REST controllers.

## Documentation

| Doc | Purpose |
|-----|---------|
| [docs/PRODUCT_DEVELOPMENT_PLAN.md](docs/PRODUCT_DEVELOPMENT_PLAN.md) | Canonical product contract, phases 0–11, failure modes |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Multi-module layout, packages, auto-config, request path |
| [docs/CHEAT_SHEET.md](docs/CHEAT_SHEET.md) | Build commands, Maven coords, config keys, HTTP smoke |
| [docs/RUNBOOK.md](docs/RUNBOOK.md) | Operational checks and audit posture (MVP) |

## Quick API smoke (demo)

With the app running on port 8080:

```bash
curl -s -X POST http://localhost:8080/agent/execute \
  -H 'Content-Type: application/json' \
  -d '{"principalId":"u1","roles":["user"],"message":"hello"}'
```

Then `GET /audit/{traceId}` using the `traceId` from the JSON response. Details: [docs/CHEAT_SHEET.md](docs/CHEAT_SHEET.md).

## License

Licensed under the **Apache License 2.0** — see [LICENSE](LICENSE).

## Open-source repository

To host the code publicly (for example on GitHub):

1. If you do not have a repo yet: `git init -b main`, then `git add -A` and commit.
2. Set your identity once: `git config user.name "Your Name"` and `git config user.email "you@example.com"` (then `git commit --amend --reset-author --no-edit` on the first commit if you used a placeholder author).
3. Create a **public** repository on GitHub (or GitLab), add the remote, push: `git remote add origin <url>` then `git push -u origin main`

CI (`.github/workflows/ci.yml`) runs `mvn verify` on pushes and pull requests.

**Maven Central** (so others resolve `com.framework.agent` without building from source) is a separate step: you must register your `groupId`, sign releases, and run `mvn deploy` to a staging repository. Until then, consumers can `git clone` and `mvn install`, or depend on a **GitHub Packages** / internal repository if you configure `distributionManagement`.
