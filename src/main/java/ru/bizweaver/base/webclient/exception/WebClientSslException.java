package ru.bizweaver.base.webclient.exception;

public class WebClientSslException extends RuntimeException {
    private static final String SSL_SETTINGS_ERROR_MESSAGE = "Ошибка при конфигурировании SSL: %s";

    public WebClientSslException(Throwable cause) {
        super(SSL_SETTINGS_ERROR_MESSAGE.formatted(cause.getMessage()), cause);
    }
}
