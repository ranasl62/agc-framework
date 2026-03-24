package com.framework.agent.audit;

/**
 * Audit durability behavior on the governed path.
 */
public enum AuditMode {
    /** Fail closed if any required audit write fails (default). */
    STRICT,
    /** Log audit failures on the governed path without blocking execution (not for regulated production). */
    BEST_EFFORT,
    /**
     * Queue-based, non-blocking governed-path writes (in-memory executor; ordering vs tool execution not guaranteed).
     */
    ASYNC
}
