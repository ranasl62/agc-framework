package com.framework.agent.autoconfigure;

import com.framework.agent.core.LlmClient;
import com.framework.agent.orchestrator.AgentOrchestrator;
import com.framework.agent.orchestrator.EchoLlmClient;
import com.framework.agent.orchestrator.LlmStubProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@AutoConfigureAfter(AgcMcpAutoConfiguration.class)
@EnableConfigurationProperties(LlmStubProperties.class)
@ComponentScan(basePackageClasses = AgentOrchestrator.class)
public class AgcOrchestratorAutoConfiguration {

    @Bean
    public LlmClient llmClient(LlmStubProperties properties) {
        String planned = properties.getPlannedToolName();
        return new EchoLlmClient(planned != null && !planned.isBlank() ? planned : "search");
    }
}
