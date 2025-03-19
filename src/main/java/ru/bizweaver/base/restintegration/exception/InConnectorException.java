package ru.bizweaver.base.restintegration.exception;

import org.camunda.bpm.engine.delegate.BpmnError;

public class InConnectorException extends BpmnError {

    public static final String SUFFIX = "BAD_REQUEST";
    private static final String CODE_PATTERN = "%s_%s, exception message: %s";

    public InConnectorException(String code, String message) {
        super(CODE_PATTERN.formatted(code, SUFFIX, message));
    }
}
