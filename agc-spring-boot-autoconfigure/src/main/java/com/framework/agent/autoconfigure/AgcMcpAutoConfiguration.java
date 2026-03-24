package com.framework.agent.autoconfigure;

import com.framework.agent.audit.AgcAuditProperties;
import com.framework.agent.audit.AuditMode;
import com.framework.agent.core.AuditRecorder;
import com.framework.agent.core.GovernancePipeline;
import com.framework.agent.core.GuardrailEvaluator;
import com.framework.agent.core.PolicyEvaluator;
import com.framework.agent.core.ToolInvocationGateway;
import com.framework.agent.core.ToolRegistry;
import com.framework.agent.mcp.AgcRuntimeProperties;
import com.framework.agent.mcp.DefaultGovernancePipeline;
import com.framework.agent.mcp.DefaultToolInvocationGateway;
import com.framework.agent.mcp.DefaultToolRegistry;
import com.framework.agent.mcp.internal.EchoMcpToolExecutor;
import com.framework.agent.mcp.internal.McpToolExecutor;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Order: Storage → Audit → Policy → Guardrail → ToolRegistry (bean) → MCP gateway → Orchestrator → Observability.
 */
@AutoConfiguration
@AutoConfigureAfter({
        AgcStorageAutoConfiguration.class,
        AgcAuditAutoConfiguration.class,
        AgcPolicyAutoConfiguration.class,
        AgcGuardrailAutoConfiguration.class
})
@EnableConfigurationProperties(AgcRuntimeProperties.class)
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
    @ConditionalOnMissingBean(ToolRegistry.class)
    public ToolRegistry toolRegistry(AgcRuntimeProperties runtimeProperties) {
        return new DefaultToolRegistry(runtimeProperties);
    }

    /**
     * Bounded queue + pool for {@link AuditMode#ASYNC}; governed-path writes are submitted without blocking the gateway thread.
     */
    @Bean(name = "agcAuditAsyncExecutor", destroyMethod = "shutdown")
    @ConditionalOnProperty(prefix = "agc.audit", name = "mode", havingValue = "ASYNC")
    public ExecutorService agcAuditAsyncExecutor() {
        return Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "agc-audit-async");
            t.setDaemon(true);
            return t;
        });
    }

    @Bean
    @ConditionalOnMissingBean(ToolInvocationGateway.class)
    public ToolInvocationGateway toolInvocationGateway(
            GovernancePipeline pipeline,
            McpToolExecutor mcpToolExecutor,
            AuditRecorder auditRecorder,
            AgcAuditProperties auditProperties,
            AgcRuntimeProperties runtimeProperties,
            ToolRegistry toolRegistry,
            @Autowired(required = false) @Qualifier("agcAuditAsyncExecutor") Executor agcAuditAsyncExecutorBean,
            ObjectProvider<MeterRegistry> meterRegistry
    ) {
        Executor async = null;
        if (auditProperties.getMode() == AuditMode.ASYNC) {
            if (agcAuditAsyncExecutorBean == null) {
                throw new IllegalStateException(
                        "agc.audit.mode=ASYNC requires bean agcAuditAsyncExecutor (auto-configured when mode=ASYNC)"
                );
            }
            async = agcAuditAsyncExecutorBean;
        }
        return new DefaultToolInvocationGateway(
                pipeline,
                mcpToolExecutor,
                auditRecorder,
                auditProperties,
                runtimeProperties,
                toolRegistry,
                async,
                meterRegistry.getIfAvailable()
        );
    }
}
