package ru.bizweaver.base.restintegration.exception;

import org.camunda.bpm.engine.delegate.BpmnError;

public class SetErrorResultException extends BpmnError {

    public static final String SUFFIX = "SET_ERROR_RESULT";
    private static final String CODE_PATTERN = "%s_%s";

    public SetErrorResultException(String code) {
        super(CODE_PATTERN.formatted(code, SUFFIX));
    }
}
