package com.framework.agent.mcp;

import com.framework.agent.core.GovernanceMode;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Global runtime switches and tool allowlist (second line of defense after policy).
 */
@ConfigurationProperties(prefix = "agc")
public class AgcRuntimeProperties {

    /**
     * Global kill switch: when false, the gateway denies all tool invocations safely.
     */
    private boolean enabled = true;

    private final Governance governance = new Governance();

    private final Tools tools = new Tools();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Tools getTools() {
        return tools;
    }

    public Governance getGovernance() {
        return governance;
    }

    public static class Governance {
        private GovernanceMode mode = GovernanceMode.DEVELOPMENT;

        public GovernanceMode getMode() {
            return mode;
        }

        public void setMode(GovernanceMode mode) {
            this.mode = mode != null ? mode : GovernanceMode.DEVELOPMENT;
        }
    }

    public static class Tools {
        /**
         * If non-empty, tool name must match (case-insensitive logical name; see {@link com.framework.agent.core.ToolNames}) before policy/guardrails.
         * If empty, no extra registry filter (policy remains authoritative).
         */
        private List<String> allowed = new ArrayList<>();

        public List<String> getAllowed() {
            return allowed;
        }

        public void setAllowed(List<String> allowed) {
            this.allowed = allowed != null ? allowed : new ArrayList<>();
        }
    }
}
