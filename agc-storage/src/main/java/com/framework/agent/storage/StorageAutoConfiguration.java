package com.framework.agent.storage;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@ComponentScan(basePackageClasses = TraceSequenceAllocator.class)
@EnableJpaRepositories(basePackageClasses = AuditEventRepository.class)
@EntityScan(basePackageClasses = AuditEventEntity.class)
public class StorageAutoConfiguration {
}
