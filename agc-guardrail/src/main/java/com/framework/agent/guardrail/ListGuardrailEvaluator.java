package com.framework.agent.guardrail;

import com.framework.agent.core.GovernanceDecision;
import com.framework.agent.core.GuardrailEvaluator;
import com.framework.agent.core.ToolInvocationContext;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ListGuardrailEvaluator implements GuardrailEvaluator {

    private final List<GuardrailBindingProperties.Rule> rules;

    public ListGuardrailEvaluator(List<GuardrailBindingProperties.Rule> rules) {
        this.rules = List.copyOf(rules);
    }

    @Override
    public GovernanceDecision evaluate(ToolInvocationContext ctx) {
        GovernanceDecision accumulatedWarn = null;
        for (GuardrailBindingProperties.Rule rule : rules) {
            if (!matches(rule, ctx.toolName())) {
                continue;
            }
            String id = rule.getId() != null ? rule.getId() : "unnamed";
            String action = rule.getAction() == null ? "DENY" : rule.getAction().trim().toUpperCase(Locale.ROOT);
            if ("DENY".equals(action)) {
                return GovernanceDecision.deny(
                        "GUARDRAIL_" + id,
                        List.of(id),
                        Map.of("tool", ctx.toolName())
                );
            }
            if ("WARN".equals(action)) {
                GovernanceDecision w = GovernanceDecision.warn(
                        "GUARDRAIL_" + id,
                        List.of(id),
                        Map.of("tool", ctx.toolName())
                );
                accumulatedWarn = accumulatedWarn == null ? w : GovernanceDecision.mergeWarn(accumulatedWarn, w);
            }
        }
        return accumulatedWarn;
    }

    private static boolean matches(GuardrailBindingProperties.Rule rule, String toolName) {
        String pattern = rule.getToolName();
        if (pattern == null || pattern.isBlank()) {
            return false;
        }
        if ("*".equals(pattern)) {
            return true;
        }
        return pattern.equalsIgnoreCase(toolName);
    }
}
