package com.framework.agent.audit;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "agc.audit")
public class AgcAuditProperties {

    private int maxPayloadChars = 4000;

    /**
     * When true (default), failure to persist SYSTEM_ERROR after a tool failure aborts with
     * {@link com.framework.agent.core.GovernedPathAuditException} when mode is {@link AuditMode#STRICT}.
     */
    private boolean strictSecondaryAudit = true;

    /**
     * {@link AuditMode#STRICT}: fail closed on audit errors on the governed path.
     * {@link AuditMode#BEST_EFFORT}: log and continue (not for regulated production).
     * {@link AuditMode#ASYNC}: submit writes to {@code agcAuditAsyncExecutor} without blocking the gateway thread.
     */
    private AuditMode mode = AuditMode.STRICT;

    public int getMaxPayloadChars() {
        return maxPayloadChars;
    }

    public void setMaxPayloadChars(int maxPayloadChars) {
        this.maxPayloadChars = maxPayloadChars;
    }

    public boolean isStrictSecondaryAudit() {
        return strictSecondaryAudit;
    }

    public void setStrictSecondaryAudit(boolean strictSecondaryAudit) {
        this.strictSecondaryAudit = strictSecondaryAudit;
    }

    public AuditMode getMode() {
        return mode;
    }

    public void setMode(AuditMode mode) {
        this.mode = mode;
    }
}
