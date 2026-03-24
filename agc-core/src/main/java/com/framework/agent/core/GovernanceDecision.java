package com.framework.agent.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable governance outcome. {@code ALLOW} with empty rule ids means policy/guardrails passed without a specific rule hit.
 */
public record GovernanceDecision(
        DecisionType type,
        String reasonCode,
        List<String> matchedRuleIds,
        Map<String, String> details
) {
    public GovernanceDecision {
        Objects.requireNonNull(type, "type");
        reasonCode = reasonCode != null ? reasonCode : "";
        matchedRuleIds = matchedRuleIds != null ? List.copyOf(matchedRuleIds) : List.of();
        details = details != null ? Map.copyOf(details) : Map.of();
    }

    public static GovernanceDecision allow() {
        return new GovernanceDecision(DecisionType.ALLOW, "AGC_ALLOW", List.of(), Map.of());
    }

    public static GovernanceDecision deny(String reasonCode, List<String> matchedRuleIds, Map<String, String> details) {
        return new GovernanceDecision(DecisionType.DENY, reasonCode, matchedRuleIds, details);
    }

    public static GovernanceDecision warn(String reasonCode, List<String> matchedRuleIds, Map<String, String> details) {
        return new GovernanceDecision(DecisionType.WARN, reasonCode, matchedRuleIds, details);
    }

    public boolean isDeny() {
        return type == DecisionType.DENY;
    }

    public boolean blocksExecution() {
        return type == DecisionType.DENY;
    }

    public static GovernanceDecision mergeWarn(GovernanceDecision first, GovernanceDecision second) {
        var ids = new ArrayList<String>();
        ids.addAll(first.matchedRuleIds());
        ids.addAll(second.matchedRuleIds());
        var det = new LinkedHashMap<String, String>();
        det.putAll(first.details());
        det.putAll(second.details());
        return warn(first.reasonCode() + "," + second.reasonCode(), List.copyOf(ids), Collections.unmodifiableMap(det));
    }
}
