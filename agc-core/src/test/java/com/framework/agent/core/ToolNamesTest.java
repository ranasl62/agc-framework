package com.framework.agent.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void validatesToolNameFormatWithVersioning() {
        assertTrue(ToolNames.isValid("search"));
        assertTrue(ToolNames.isValid("search:v1"));
        assertTrue(ToolNames.isValid("payment_api:v22"));
        assertFalse(ToolNames.isValid("search:vx"));
        assertFalse(ToolNames.isValid("tool:with:colons"));
        assertFalse(ToolNames.isValid(""));
    }
}
