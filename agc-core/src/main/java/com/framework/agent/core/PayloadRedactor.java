package com.framework.agent.core;

@FunctionalInterface
public interface PayloadRedactor {

    String redactAndBound(String raw, int maxChars);
}
