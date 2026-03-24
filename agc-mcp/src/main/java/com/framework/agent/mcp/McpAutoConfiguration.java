package com.framework.agent.mcp;

import com.framework.agent.core.AuditRecorder;
import com.framework.agent.core.GovernancePipeline;
import com.framework.agent.core.GuardrailEvaluator;
import com.framework.agent.core.McpToolExecutor;
import com.framework.agent.core.PolicyEvaluator;
import com.framework.agent.core.ToolInvocationGateway;
import com.framework.agent.audit.AuditAutoConfiguration;
import com.framework.agent.guardrail.GuardrailAutoConfiguration;
import com.framework.agent.policy.PolicyAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@AutoConfigureAfter({PolicyAutoConfiguration.class, GuardrailAutoConfiguration.class, AuditAutoConfiguration.class})
public class McpAutoConfiguration {

    @Bean
    public GovernancePipeline governancePipeline(PolicyEvaluator policy, GuardrailEvaluator guardrails) {
        return new DefaultGovernancePipeline(policy, guardrails);
    }

    @Bean
    public McpToolExecutor mcpToolExecutor() {
        return new EchoMcpToolExecutor();
    }

    @Bean
    public ToolInvocationGateway toolInvocationGateway(
            GovernancePipeline pipeline,
            McpToolExecutor mcpToolExecutor,
            AuditRecorder auditRecorder
    ) {
        return new DefaultToolInvocationGateway(pipeline, mcpToolExecutor, auditRecorder);
    }
}
