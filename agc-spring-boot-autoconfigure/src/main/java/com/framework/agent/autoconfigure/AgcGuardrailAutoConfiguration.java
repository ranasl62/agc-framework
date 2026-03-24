package com.framework.agent.autoconfigure;

import com.framework.agent.core.GuardrailEvaluator;
import com.framework.agent.guardrail.GuardrailBindingProperties;
import com.framework.agent.guardrail.ListGuardrailEvaluator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@AutoConfigureAfter(AgcPolicyAutoConfiguration.class)
@EnableConfigurationProperties(GuardrailBindingProperties.class)
public class AgcGuardrailAutoConfiguration {

    @Bean
    public GuardrailEvaluator guardrailEvaluator(GuardrailBindingProperties properties) {
        return new ListGuardrailEvaluator(properties.getRules());
    }
}
