package ru.bizweaver.base.restapi.exception;

/**
 * Исключение, связанное с привешением времени ожидания ответа на запрос.
 *
 * @author NTB Team
 */
public class RequestTimeoutException extends RuntimeException {
    public RequestTimeoutException() {
        super();
    }
}
