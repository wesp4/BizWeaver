package ru.bizweaver.base.restapi.exception;

/**
 * Ошибка отсутствия асинхронного ответа
 *
 * @author NTB Team
 */
public class AsyncResponseNotFoundException extends RuntimeException {
    public AsyncResponseNotFoundException() {
        super();
    }
}
