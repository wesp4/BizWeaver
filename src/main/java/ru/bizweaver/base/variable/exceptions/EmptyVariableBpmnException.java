package ru.bizweaver.base.variable.exceptions;

import org.camunda.bpm.engine.delegate.BpmnError;

/**
 * Исключение, связанное с пустым значением переменной.
 *
 * @author NTB Team
 */
public class EmptyVariableBpmnException extends BpmnError {

    public static final String MESSAGE_PATTERN = "EMPTY_%s_VARIABLE";

    public EmptyVariableBpmnException(String variableName) {
        super(MESSAGE_PATTERN.formatted(variableName));
    }
}
