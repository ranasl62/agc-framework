package com.framework.agent.core;

/**
 * Deployment posture for HTTP entry points. {@link #PRODUCTION} requires an authenticated principal
 * when calling governed APIs (enforced in {@code agc-api}).
 */
public enum GovernanceMode {
    /** Local / CI; request-body identity allowed for demos. */
    DEVELOPMENT,
    /** Stricter checks may be enabled by integrators; same HTTP rules as DEVELOPMENT in this framework. */
    STAGING,
    /** Authenticated principal required for {@code /agent/execute}. */
    PRODUCTION
}
