package ru.bizweaver.base.restintegration.exception;

import org.camunda.bpm.engine.delegate.BpmnError;

public class UnknownIntegrationException extends BpmnError {

    public static final String SUFFIX = "UNKNOWN_INTEGRATION_ERROR";
    private static final String CODE_PATTERN = "%s_%s";

    public UnknownIntegrationException(String code, Throwable cause) {
        super(CODE_PATTERN.formatted(code, SUFFIX), cause);
    }
}
