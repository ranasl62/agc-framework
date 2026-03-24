package com.framework.agent.policy;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "agc.policy")
public class PolicyBindingProperties {

    private Map<String, List<String>> roles = new LinkedHashMap<>();

    public Map<String, List<String>> getRoles() {
        return roles;
    }

    public void setRoles(Map<String, List<String>> roles) {
        this.roles = roles != null ? new LinkedHashMap<>(roles) : new LinkedHashMap<>();
    }
}
