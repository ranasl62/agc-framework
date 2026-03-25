# Governance model

How AGC classifies tool invocations before execution. Execution goes through **ToolInvocationGateway** and the **governance pipeline** (registry, then policy, then guardrails).

## Decision types

| Type | Meaning |
|------|---------|
| **ALLOW** | Tool may run after registry, policy, and guardrails. |
| **DENY** | Tool does not run; **reasonCode** explains why. |
| **WARN** | Guardrail matched with warn semantics; tool may still run per rules. |

The pipeline must not return a null decision.

## Policy model

- **Roles** come from **ToolInvocationContext** (REST: Spring Security when authenticated; otherwise request for dev-style flows).
- **Tools** use **logical names** via **ToolNames.logicalName** (e.g. **search:v2** matches policy for **search**).
- **Configuration:** **agc.policy.roles** maps role to allowed tools, or **"*"** for all (still subject to registry and guardrails).

## Reason codes (representative)

| Code | Typical cause |
|------|----------------|
| TOOL_NOT_REGISTERED | Tool not in **agc.tools.allowed** when allowlist is non-empty. |
| POLICY_TOOL_FORBIDDEN | Role set does not allow the tool. |
| POLICY_NO_ROLES | No roles on context. |
| AGC_DISABLED | **agc.enabled: false**. |
| GOVERNANCE_EVALUATION_FAILED | Policy or guardrail evaluator threw. |
| GUARDRAIL_* | Guardrail DENY (e.g. GUARDRAIL_block-payment). |
| INVALID_CONTEXT | Missing trace/correlation or invalid tool name. |
| AUTH_REQUIRED | PRODUCTION governance without authenticated principal (REST). |
| AUDIT_FAILURE | Governed-path audit failed in STRICT mode. |

HTTP denial shape: see README. Call order, context fields, and exception mapping: **docs/USAGE.md**. Config reference: **docs/ARCHITECTURE.md**.

## Tool registry

**agc.tools.allowed** — empty means no extra registry filter; non-empty means allowlist runs before policy.

## REST vs embedded

- **agc-api**: **POST /agent/execute** maps denials to 403 with **decision**, **reasonCode**, **matchedRuleIds**.
- Embedded: call **ToolInvocationGateway** from your orchestrator; same pipeline applies.
