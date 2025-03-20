package ru.bizweaver.base.variable.exceptions;

import org.camunda.bpm.engine.delegate.BpmnError;

public class EmptyArrayVariableBpmnException extends BpmnError {

    public static final String MESSAGE_PATTERN = "EMPTY_ARRAY_%s_VARIABLE";

    public EmptyArrayVariableBpmnException(String variableName) {
        super(MESSAGE_PATTERN.formatted(variableName));
    }
}
