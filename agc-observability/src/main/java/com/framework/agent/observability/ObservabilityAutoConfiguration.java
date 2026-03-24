package com.framework.agent.observability;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

/**
 * Applies common Micrometer tags when a {@link MeterRegistry} exists (e.g. with actuator).
 * OpenTelemetry wiring can be added here later without forcing optional deps on all consumers.
 */
@AutoConfiguration
@ConditionalOnClass(MeterRegistry.class)
@ConditionalOnBean(MeterRegistry.class)
public class ObservabilityAutoConfiguration {

    public ObservabilityAutoConfiguration(MeterRegistry registry) {
        registry.config().commonTags("framework", "agc");
    }
}
