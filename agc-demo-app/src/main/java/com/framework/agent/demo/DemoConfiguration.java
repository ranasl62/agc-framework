package com.framework.agent.demo;

import com.framework.agent.core.LlmClient;
import com.framework.agent.orchestrator.LlmStubProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DemoConfiguration {

    @Bean
    @Primary
    public LlmClient demoLlmClient(LlmStubProperties properties) {
        return new DemoLlmClient(properties);
    }
}
