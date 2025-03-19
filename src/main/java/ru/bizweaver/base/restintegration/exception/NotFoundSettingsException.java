package ru.bizweaver.base.restintegration.exception;

public class NotFoundSettingsException extends RuntimeException {

    private static final String MESSAGE = "Not found settings for: %s";

    public NotFoundSettingsException(String serviceName) {
        super(MESSAGE.formatted(serviceName));
    }
}
