package com.framework.agent.orchestrator;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "agc.llm")
public class LlmStubProperties {

    private String plannedToolName = "search";

    public String getPlannedToolName() {
        return plannedToolName;
    }

    public void setPlannedToolName(String plannedToolName) {
        this.plannedToolName = plannedToolName;
    }
}
