package com.framework.agent.autoconfigure;

import com.framework.agent.audit.AgcAuditProperties;
import com.framework.agent.audit.JpaAuditRecorder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@AutoConfigureAfter(AgcStorageAutoConfiguration.class)
@EnableConfigurationProperties(AgcAuditProperties.class)
@ComponentScan(basePackageClasses = JpaAuditRecorder.class)
public class AgcAuditAutoConfiguration {
}
