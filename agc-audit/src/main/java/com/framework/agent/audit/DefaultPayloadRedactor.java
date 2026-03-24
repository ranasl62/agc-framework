package com.framework.agent.audit;

import com.framework.agent.core.PayloadRedactor;
import org.springframework.stereotype.Component;

@Component
public class DefaultPayloadRedactor implements PayloadRedactor {

    @Override
    public String redactAndBound(String raw, int maxChars) {
        if (raw == null) {
            return "";
        }
        if (raw.length() <= maxChars) {
            return raw;
        }
        return raw.substring(0, maxChars) + "…[truncated]";
    }
}
