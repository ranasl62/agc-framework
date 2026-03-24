---
description: AGC Phase 1 — Multi-module layout (core pure Java, autoconfigure, starter aggregator, storage, audit, policy, guardrail, mcp, orchestrator)
---

Implement or extend **Phase 1 — Bootstrap** for Agent Governance & Control.

**Module layout (required):**
- **agc-core** — pure Java, `com.framework.agent.core` only (no Spring)
- **agc-storage**, **agc-audit**, **agc-policy**, **agc-guardrail**, **agc-mcp**, **agc-orchestrator**, **agc-observability**, **agc-api** (REST)
- **agc-spring-boot-autoconfigure** — all `@AutoConfiguration` classes + single **`META-INF/spring/.../AutoConfiguration.imports`**
- **agc-spring-boot-starter** — aggregator only (depends on autoconfigure)
- **agc-architecture-tests** — ArchUnit governance rules (optional but recommended)
- **agc-demo-app** — sample

**Requirements:**
- **`agc-spring-boot-starter`** is the single dependency users add for headless runtime; **agc-api** optional for REST
- **`McpToolExecutor`** lives in **`com.framework.agent.mcp.internal`**; only **`DefaultToolInvocationGateway`** (and autoconfigure wiring) may use it
- **`ToolInvocationGateway`** bean wired in **`AgcMcpAutoConfiguration`**
- Document **unsupported:** direct MCP for governed tools

See [README.md](../../README.md) and [docs/ARCHITECTURE.md](../../docs/ARCHITECTURE.md).
