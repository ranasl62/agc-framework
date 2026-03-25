# Quickstart

Minimal path to a running app with AGC and optional REST.

Dependencies below use **`1.0.0`** (release). For unreleased source builds, run `mvn install` and use a `SNAPSHOT` version, or see [RELEASING.md](RELEASING.md).

---

## 1. Dependencies

```xml
<dependency>
  <groupId>com.framework.agent</groupId>
  <artifactId>agc-spring-boot-starter</artifactId>
  <version>1.0.0</version>
</dependency>
<dependency>
  <groupId>com.framework.agent</groupId>
  <artifactId>agc-api</artifactId>
  <version>1.0.0</version>
</dependency>
```

Use `1.0.0` after [Maven Central](https://search.maven.org/) publication. Build locally: `mvn clean install` from the AGC repo.

---

## 2. Config

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:quick;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE
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

---

## 3. Run and call

```bash
mvn spring-boot:run
```

```bash
curl -s -X POST http://localhost:8080/agent/execute \
  -H 'Content-Type: application/json' \
  -d '{"traceId":"q-1","correlationId":"q-1","principalId":"u1","roles":["user"],"message":"[[tool:search]]"}'
```

**Success:** HTTP 200, body includes `outcomeSummary` (your `McpToolExecutor`; the demo app uses summaries like `demo-backend.search …`) and `success: true`.

**Deny:** HTTP 403, JSON includes `decision`, `reasonCode`, `matchedRuleIds`.

**Audit trail:** `GET http://localhost:8080/audit/q-1` — events ordered by `sequenceNum`.

---

## 4. Next

- [USAGE.md](USAGE.md) — how each feature works, call sequence, storing/passing data, exceptions  
- [ARCHITECTURE.md](ARCHITECTURE.md) — module graph, all `agc.*` keys  
- [GOVERNANCE.md](GOVERNANCE.md) — reason codes and policy  
- [FAILURE_MODES.md](FAILURE_MODES.md) — STRICT vs ASYNC vs BEST_EFFORT  
