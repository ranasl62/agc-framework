package com.framework.agent.api.web;

import com.framework.agent.orchestrator.OrchestratorAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@AutoConfigureAfter(OrchestratorAutoConfiguration.class)
@ComponentScan(basePackageClasses = AgentExecuteController.class)
public class AgcWebAutoConfiguration {
}
