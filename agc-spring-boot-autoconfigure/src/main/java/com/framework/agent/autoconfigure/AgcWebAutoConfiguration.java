package com.framework.agent.autoconfigure;

import com.framework.agent.api.web.AgentExecuteController;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@AutoConfigureAfter({AgcOrchestratorAutoConfiguration.class, AgcObservabilityAutoConfiguration.class})
@ConditionalOnClass(AgentExecuteController.class)
@ComponentScan(basePackageClasses = AgentExecuteController.class)
public class AgcWebAutoConfiguration {
}
