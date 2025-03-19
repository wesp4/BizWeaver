package ru.bizweaver.base.restapi.exception;

/**
 * Исключение, связанное с некорректным бизнес ключем полученным в запросе.
 *
 * @author NTB Team
 */
public class BadBusinessKeyException extends RuntimeException {
    public BadBusinessKeyException() {
        super();
    }
}
