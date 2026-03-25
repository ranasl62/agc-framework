# Failure modes

Behavior when persistence, evaluators, or the tool adapter fail. Default: **agc.audit.mode: STRICT**.

Step-by-step flow and HTTP exception mapping: [USAGE.md](USAGE.md).

## Audit modes

| Mode | Governed-path write fails |
|------|---------------------------|
| STRICT | Fail closed (GovernedPathAuditException; REST 503 where wired). |
| BEST_EFFORT | Logged; execution may continue. |
| ASYNC | Bounded queue; enqueue failure fails closed; async persist errors logged. |

**SYSTEM_ERROR** after tool failure respects **agc.audit.strict-secondary-audit** (default true) in STRICT.

## Database unavailable (STRICT)

If audit cannot be written on the governed path, the request is not treated as successful and execution does not complete safely from a compliance perspective.

## Policy or guardrail throws

Wrapped as DENY with **GOVERNANCE_EVALUATION_FAILED**. No silent allow.

## Tool adapter fails

After ALLOW, **SYSTEM_ERROR** audit is attempted; STRICT + strict secondary audit can fail the request if that write fails.

## Kill switch

**agc.enabled: false** denies with **AGC_DISABLED**; audit follows the active audit mode.

## See also

- **docs/ARCHITECTURE.md** — configuration keys
- **docs/GOVERNANCE.md** — decisions and reason codes
