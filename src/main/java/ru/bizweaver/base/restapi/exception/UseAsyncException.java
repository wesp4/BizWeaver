package ru.bizweaver.base.restapi.exception;


import lombok.Getter;
import ru.bizweaver.base.restapi.service.model.CheckAcceptedData;

/**
 * Исключение, связанное с пустым значением переменной.
 *
 * @author NTB Team
 */
@Getter
public class UseAsyncException extends RuntimeException {

    private final CheckAcceptedData checkAcceptedData;

    public UseAsyncException(CheckAcceptedData checkAcceptedData) {
        super();
        this.checkAcceptedData = checkAcceptedData;
    }

    public String getCallId() {
        return this.checkAcceptedData.getRequestId();
    }
}
