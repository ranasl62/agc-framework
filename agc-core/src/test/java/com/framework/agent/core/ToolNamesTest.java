package com.framework.agent.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ToolNamesTest {

    @Test
    void logicalNameStripsVersionSuffix() {
        assertEquals("search", ToolNames.logicalName("search:v1"));
        assertEquals("search", ToolNames.logicalName("Search:V2"));
    }

    @Test
    void logicalNamePreservesUnversioned() {
        assertEquals("payment_api", ToolNames.logicalName("payment_api"));
        assertEquals("tool:with:colons", ToolNames.logicalName("tool:with:colons"));
    }
}
