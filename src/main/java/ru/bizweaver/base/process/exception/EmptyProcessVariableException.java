package ru.bizweaver.base.process.exception;

public class EmptyProcessVariableException extends RuntimeException {

    private static final String MESSAGE = "EMPTY_PROCESS_VARIABLE_EXCEPTION";
    private String name;
    private String businessKey;

    public EmptyProcessVariableException(String name, String businessKey) {
        super(MESSAGE);
    }
}
