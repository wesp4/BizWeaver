package ru.bizweaver.base.restapi.model;


import lombok.Data;
import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class AsyncResponse<T> implements Serializable {
    /**
     * Serial version UID.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private HttpStatus status;
    private List<ErrorContainer> errors;
    private T successResponse;

    public AsyncResponse(HttpStatus status) {
        this.status = status;
    }

    public AsyncResponse(HttpStatus status, List<ErrorContainer> errors) {
        this.status = status;
        this.errors = errors;
    }

    public AsyncResponse(T successResponse) {
        this.status = HttpStatus.OK;
        this.successResponse = successResponse;
    }
}
