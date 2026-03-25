# AGC — Library guide (Maven Central & adoption)

This page is the **canonical reference** for what the project is called, **why it exists**, **who it is for**, and **how people can find it** once you publish to Maven Central and GitHub.

---

## Official name and coordinates

| | |
|--|--|
| **Product name** | **Agent Governance & Control** |
| **Short name** | **AGC** |
| **Maven `groupId`** | `com.framework.agent` |
| **Primary consumer artifact** | `agc-spring-boot-starter` |
| **Optional REST module** | `agc-api` |
| **Repository / homepage** | [github.com/ranasl62/agc-framework](https://github.com/ranasl62/agc-framework) (adjust if your fork is canonical) |

**Typical dependency (after release):**

```xml
<dependency>
  <groupId>com.framework.agent</groupId>
  <artifactId>agc-spring-boot-starter</artifactId>
  <version>1.0.0</version>
</dependency>
```

Add `agc-api` if you want `POST /agent/execute` and `GET /audit/{traceId}` without writing controllers yourself.

---

## Why this library matters

Modern stacks make it easy to **call models** and **expose tools**. They rarely give you, out of the box:

- A **single, non-bypassable execution path** for every governed tool call  
- **Role- and policy-based** allow/deny with stable **`reasonCode`s**  
- **Guardrails** (e.g. block payment tools for everyone, warn on sensitive reads)  
- An **append-only audit trail** keyed by **`traceId`**, suitable for security and compliance reviews  

**AGC fills that gap on the JVM:** it is a **control plane for tool execution** in Spring Boot applications—not a replacement for Spring AI, LangChain4j, or vendor SDKs, but a **governance and audit layer** you can place **in front of** whatever actually runs your tools (HTTP, MCP adapter, in-process services, etc.).

**Importance in one line:** *If an agent can call production systems, “who can do what” and “what happened” must be as engineered as the prompt—and AGC makes that explicit in code and storage.*

---

## Who needs AGC

Use it when you:

- Ship **AI agents or copilots** that invoke **backend tools** (databases, APIs, internal actions).  
- Answer **security, risk, or compliance** questions about tool use (**audit**, **deny reasons**, **separation of duties**).  
- Want **fail-closed** behavior when governance or mandatory audit cannot complete.  
- Prefer **declarative policy** (YAML/properties) over scattered `if` statements around each integration.  

You may **not** need it for a one-off demo with a single hard-coded tool and no audit requirements—though the same patterns often appear once that demo goes to staging.

---

## How people can find it (search & discovery)

### Maven Central

Users often search by **groupId**, **artifactId**, or **words in the POM description**. After deployment, typical searches:

- **By coordinates:** `g:com.framework.agent`  
- **By artifact:** `a:agc-spring-boot-starter`  
- **Full coordinates:** `com.framework.agent:agc-spring-boot-starter`  

Central search: [search.maven.org — `com.framework.agent`](https://search.maven.org/search?q=g:com.framework.agent)

### GitHub (and other hosts)

Recommend adding **repository topics** (Settings → General → Topics), for example:

`spring-boot` · `java` · `llm` · `ai-agents` · `tool-calling` · `governance` · `audit` · `compliance` · `policy` · `authorization` · `mcp` · `enterprise-ai` · `observability`

### Keywords and phrases (SEO-style)

These are phrases maintainers and users might type into Google, Maven Central, or GitHub search. They are intentionally broad; the README and POM description embed several of them.

| Category | Example search terms |
|----------|----------------------|
| **Problem** | LLM tool governance Java, agent tool authorization, AI tool audit log, prevent unauthorized tool calls |
| **Stack** | Spring Boot agent framework, Java LLM tools, JVM tool invocation gateway |
| **Policy** | role-based tool access, guardrails for AI tools, policy engine for agents |
| **Compliance** | SOX-style tool audit (conceptual), explainable AI tool denial, traceId audit trail |
| **Integration** | Spring AI governance (complementary), MCP tool execution Java, governed MCP adapter |

**Suggested short label for talks or articles:** *“AGC — Spring Boot governance for LLM tool execution.”*

---

## Documentation map

| Document | Audience | Content |
|----------|----------|---------|
| [README.md](../README.md) | Everyone | Overview, quickstart, badges, links |
| **This file** | Adopters & publishers | Name, importance, discovery, coordinates |
| [USAGE.md](USAGE.md) | Integrators | Features, sequential flow, data & audit, exceptions |
| [RELEASING.md](RELEASING.md) | Maintainers | Maven Central publish procedure |
| [QUICKSTART.md](QUICKSTART.md) | Developers | Minimal wiring + curl |
| [ARCHITECTURE.md](ARCHITECTURE.md) | Integrators | Modules, config, HTTP |
| [GOVERNANCE.md](GOVERNANCE.md) | Security / platform | Decisions, reason codes, policy |
| [FAILURE_MODES.md](FAILURE_MODES.md) | SRE / architects | STRICT / ASYNC / BEST_EFFORT, failures |

---

## Relationship to other Java AI libraries

- **Spring AI / LangChain4j / vendor SDKs:** model access, chat memory, RAG, tool *registration* with the model.  
- **AGC:** **enforcement** before your code runs the tool—registry, RBAC-style policy, guardrails, audit, and a **single gateway** for execution.

You can use **both**: let Spring AI (for example) propose a tool name; your application still routes **actual execution** through **`ToolInvocationGateway`** so policy and audit always apply.

---

## Publishing checklist (maintainers)

**Step-by-step guide:** **[RELEASING.md](RELEASING.md)** (OSSRH account, GPG, `settings.xml`, `mvn clean deploy -Prelease`, post-release `SNAPSHOT` bump).

Summary:

1. Reactor **release version** (currently **1.0.0** in this repo) on the tag you deploy.  
2. **Sonatype** namespace for `com.framework.agent` verified.  
3. **`settings.xml`** server **`central`** (Portal user token) + **GPG** signing.  
4. **`mvn clean deploy -Prelease`** — demo/arch-tests **skip deploy**.  
5. After Central sync, bump to **next SNAPSHOT** (e.g. `1.0.1-SNAPSHOT`) for continued development — see **RELEASING.md**.

---

## Citing the project

Suggested citation (adapt version and year):

> Agent Governance & Control (AGC). Spring Boot library for governed LLM tool execution, policy, guardrails, and audit. `com.framework.agent:agc-spring-boot-starter`. https://github.com/ranasl62/agc-framework

---

## License

Apache License 2.0 — see [LICENSE](../LICENSE).
