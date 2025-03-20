package ru.bizweaver.base.restapi.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import ru.bizweaver.base.restapi.model.ErrorContainer;

import java.util.List;


@Getter
public class ProcessHasErrorsException extends RuntimeException {
    private final HttpStatus status;
    private final List<ErrorContainer> errors;

    public ProcessHasErrorsException(HttpStatus status, List<ErrorContainer> errors) {
        super();
        this.status = status;
        this.errors = errors;
    }
}
