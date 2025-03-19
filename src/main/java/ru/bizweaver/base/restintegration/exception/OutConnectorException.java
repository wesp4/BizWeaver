package ru.bizweaver.base.restintegration.exception;

import org.camunda.bpm.engine.delegate.BpmnError;

public class OutConnectorException extends BpmnError {

    public static final String SUFFIX = "OUT_CONNECTOR";
    private static final String CODE_PATTERN = "%s_%s";

    public OutConnectorException(String code) {
        super(CODE_PATTERN.formatted(code, SUFFIX));
    }
}
