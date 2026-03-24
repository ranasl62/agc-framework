package com.framework.agent.autoconfigure;

import com.framework.agent.core.PolicyEvaluator;
import com.framework.agent.policy.PolicyBindingProperties;
import com.framework.agent.policy.RoleToolPolicyEvaluator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@AutoConfigureAfter(AgcAuditAutoConfiguration.class)
@EnableConfigurationProperties(PolicyBindingProperties.class)
public class AgcPolicyAutoConfiguration {

    @Bean
    public PolicyEvaluator policyEvaluator(PolicyBindingProperties properties) {
        return new RoleToolPolicyEvaluator(properties.getRoles());
    }
}
