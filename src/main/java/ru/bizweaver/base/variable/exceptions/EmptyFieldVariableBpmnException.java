package ru.bizweaver.base.variable.exceptions;

import org.camunda.bpm.engine.delegate.BpmnError;

/**
 * Исключение, связанное с пустым значением поля переменной.
 *
 * @author NTB Team
 */
public class EmptyFieldVariableBpmnException extends BpmnError {

    public static final String MESSAGE_PATTERN = "EMPTY_FIELD_%s_VARIABLE";

    public EmptyFieldVariableBpmnException(String variableName) {
        super(MESSAGE_PATTERN.formatted(variableName));
    }
}
