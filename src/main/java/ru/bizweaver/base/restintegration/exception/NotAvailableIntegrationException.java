package ru.bizweaver.base.restintegration.exception;

import org.camunda.bpm.engine.delegate.BpmnError;

public class NotAvailableIntegrationException extends BpmnError {

    public static final String SUFFIX = "NOT_AVAILABLE";
    private static final String CODE_PATTERN = "%s_%s";

    public NotAvailableIntegrationException(String code) {
        super(CODE_PATTERN.formatted(code, SUFFIX));
    }
}
