package com.framework.agent.guardrail;

import com.framework.agent.core.GuardrailEvaluator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(GuardrailBindingProperties.class)
public class GuardrailAutoConfiguration {

    @Bean
    public GuardrailEvaluator guardrailEvaluator(GuardrailBindingProperties properties) {
        return new ListGuardrailEvaluator(properties.getRules());
    }
}
