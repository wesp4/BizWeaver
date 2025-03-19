package ru.bizweaver.base.restintegration.exception;

import org.camunda.bpm.engine.delegate.BpmnError;

public class NotFoundOutConnectorException extends BpmnError {

    public static final String SUFFIX = "ERROR_RESPONSE";
    private static final String CODE_PATTERN = "%s_%s";

    public NotFoundOutConnectorException(String code) {
        super(CODE_PATTERN.formatted(code, SUFFIX));
    }
}
