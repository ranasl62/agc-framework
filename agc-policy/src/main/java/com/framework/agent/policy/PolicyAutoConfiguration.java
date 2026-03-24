package com.framework.agent.policy;

import com.framework.agent.core.PolicyEvaluator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(PolicyBindingProperties.class)
public class PolicyAutoConfiguration {

    @Bean
    public PolicyEvaluator policyEvaluator(PolicyBindingProperties properties) {
        return new RoleToolPolicyEvaluator(properties.getRoles());
    }
}
