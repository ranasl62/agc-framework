package com.framework.agent.autoconfigure;

import com.framework.agent.audit.AgcAuditProperties;
import com.framework.agent.core.AuditRecorder;
import com.framework.agent.core.GovernancePipeline;
import com.framework.agent.core.GuardrailEvaluator;
import com.framework.agent.core.PolicyEvaluator;
import com.framework.agent.core.ToolInvocationGateway;
import com.framework.agent.mcp.DefaultGovernancePipeline;
import com.framework.agent.mcp.DefaultToolInvocationGateway;
import com.framework.agent.mcp.internal.EchoMcpToolExecutor;
import com.framework.agent.mcp.internal.McpToolExecutor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@AutoConfigureAfter({AgcGuardrailAutoConfiguration.class, AgcAuditAutoConfiguration.class})
public class AgcMcpAutoConfiguration {

    @Bean
    public GovernancePipeline governancePipeline(PolicyEvaluator policy, GuardrailEvaluator guardrails) {
        return new DefaultGovernancePipeline(policy, guardrails);
    }

    @Bean
    @ConditionalOnMissingBean(McpToolExecutor.class)
    public McpToolExecutor mcpToolExecutor() {
        return new EchoMcpToolExecutor();
    }

    @Bean
    @ConditionalOnMissingBean(ToolInvocationGateway.class)
    public ToolInvocationGateway toolInvocationGateway(
            GovernancePipeline pipeline,
            McpToolExecutor mcpToolExecutor,
            AuditRecorder auditRecorder,
            AgcAuditProperties auditProperties
    ) {
        return new DefaultToolInvocationGateway(pipeline, mcpToolExecutor, auditRecorder, auditProperties);
    }
}
