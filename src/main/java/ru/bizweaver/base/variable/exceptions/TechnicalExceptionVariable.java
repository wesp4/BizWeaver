package ru.bizweaver.base.variable.exceptions;

import org.camunda.bpm.engine.delegate.BpmnError;

public class TechnicalExceptionVariable extends BpmnError {

    public static final String MESSAGE_PATTERN = "TECHNICAL_EXCEPTION_%s_VARIABLE, exception message: %s";

    public TechnicalExceptionVariable(String variableName, String message) {
        super(MESSAGE_PATTERN.formatted(variableName, message));
    }
}
