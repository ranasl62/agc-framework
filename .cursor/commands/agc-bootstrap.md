---
description: AGC Phase 1 — Multi-module layout (core pure Java, starter, storage, audit, policy, guardrail, mcp, orchestrator)
---

Implement or extend **Phase 1 — Bootstrap** for Agent Governance & Control.

**Module layout (required):**
- **agc-core** — pure Java, `com.framework.agent.core` only (no Spring)
- **agc-storage**, **agc-audit**, **agc-policy**, **agc-guardrail**, **agc-mcp**, **agc-orchestrator**, **agc-observability**, **agc-api** (REST), **agc-spring-boot-starter** (aggregator), **agc-demo-app**

**Requirements:**
- **`agc-spring-boot-starter`** is the single dependency users add for headless runtime; **agc-api** optional for REST
- Each submodule exposes **`META-INF/spring/...AutoConfiguration.imports`** where needed
- **`ToolInvocationGateway`** bean from **agc-mcp**
- Document **unsupported:** direct MCP for governed tools

See [README.md](../../README.md) and [docs/PRODUCT_DEVELOPMENT_PLAN.md](../../docs/PRODUCT_DEVELOPMENT_PLAN.md).
