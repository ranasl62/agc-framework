package com.framework.agent.orchestrator;

import com.framework.agent.core.LlmClient;
import com.framework.agent.mcp.McpAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@AutoConfigureAfter(McpAutoConfiguration.class)
@EnableConfigurationProperties(LlmStubProperties.class)
@ComponentScan(basePackageClasses = AgentOrchestrator.class)
public class OrchestratorAutoConfiguration {

    @Bean
    public LlmClient llmClient(LlmStubProperties properties) {
        String planned = properties.getPlannedToolName();
        return new EchoLlmClient(planned != null && !planned.isBlank() ? planned : "search");
    }
}
