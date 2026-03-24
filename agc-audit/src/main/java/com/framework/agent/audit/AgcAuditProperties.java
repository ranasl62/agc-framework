package com.framework.agent.audit;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "agc.audit")
public class AgcAuditProperties {

    private int maxPayloadChars = 4000;

    /**
     * When true (default), failure to persist SYSTEM_ERROR after a tool failure aborts with {@link com.framework.agent.core.GovernedPathAuditException}.
     */
    private boolean strictSecondaryAudit = true;

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
}
