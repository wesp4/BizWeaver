package ru.bizweaver.base.restintegration.exception;

import org.camunda.bpm.engine.delegate.BpmnError;

public class DefaultOutConnectorException extends BpmnError {

    public static final String SUFFIX = "BAD_HANDLER";
    private static final String CODE_PATTERN = "%s_%s";

    public DefaultOutConnectorException(String code) {
        super(CODE_PATTERN.formatted(code, SUFFIX));
    }
}
