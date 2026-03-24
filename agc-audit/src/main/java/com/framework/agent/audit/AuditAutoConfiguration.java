package com.framework.agent.audit;

import com.framework.agent.storage.StorageAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@AutoConfigureAfter(StorageAutoConfiguration.class)
@EnableConfigurationProperties(AgcAuditProperties.class)
@ComponentScan(basePackageClasses = JpaAuditRecorder.class)
public class AuditAutoConfiguration {
}
