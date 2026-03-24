package com.framework.agent.core;

public class ToolExecutionException extends Exception {

    public ToolExecutionException(String message) {
        super(message);
    }

    public ToolExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
