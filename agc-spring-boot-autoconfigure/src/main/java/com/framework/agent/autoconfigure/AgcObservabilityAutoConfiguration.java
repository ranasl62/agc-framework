package com.framework.agent.autoconfigure;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

/**
 * Applies common Micrometer tags when a {@link MeterRegistry} exists (e.g. with actuator).
 */
@AutoConfiguration
@AutoConfigureAfter(AgcOrchestratorAutoConfiguration.class)
@ConditionalOnClass(MeterRegistry.class)
@ConditionalOnBean(MeterRegistry.class)
public class AgcObservabilityAutoConfiguration {

    public AgcObservabilityAutoConfiguration(MeterRegistry registry) {
        registry.config().commonTags("framework", "agc");
    }
}
