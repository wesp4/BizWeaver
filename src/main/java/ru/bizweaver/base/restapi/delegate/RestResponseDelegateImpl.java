package ru.bizweaver.base.restapi.delegate;

import ru.bizweaver.base.process.model.AbstractEventContext;
import ru.bizweaver.base.restapi.connector.RestResponseConnector;
import ru.bizweaver.base.restapi.model.AsyncResponse;
import ru.bizweaver.base.restapi.service.RestApiService;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public class RestResponseDelegateImpl<T> implements RestResponseDelegate<T> {

    private final RestResponseConnector<T> restResponseConnector;
    private final RestApiService<T> restApiService;

    @Override
    public void setSuccess(DelegateExecution execution) {
        AbstractEventContext abstractEventContext =
            restApiService.getProcessService().getEventContextVariable().getNonNull(execution);
        AsyncResponse<T> asyncResponse = new AsyncResponse<>(
            restResponseConnector.getSuccessBody(execution));
        restApiService.setResponse(abstractEventContext, asyncResponse);
    }

    @Override
    public void setError(DelegateExecution execution, Integer httpCode) {
        AbstractEventContext abstractEventContext =
            restApiService.getProcessService().getEventContextVariable().getNonNull(execution);

        HttpStatus status;
        try {
            status = HttpStatus.valueOf(httpCode);
        } catch (Exception e) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        AsyncResponse<T> asyncResponse = new AsyncResponse<>(
            status,
            restResponseConnector.getErrorBody(execution));
        restApiService.setResponse(abstractEventContext, asyncResponse);
    }

    @Override
    public void saveSuccess(DelegateExecution execution) {
        AbstractEventContext abstractEventContext =
            restApiService.getProcessService().getEventContextVariable().getNonNull(execution);
        AsyncResponse<T> asyncResponse = new AsyncResponse<>(
            restResponseConnector.getSuccessBody(execution));
        execution.setVariable(abstractEventContext.getCallId(), asyncResponse);
    }

    @Override
    public void saveError(DelegateExecution execution, Integer httpCode) {
        AbstractEventContext abstractEventContext =
            restApiService.getProcessService().getEventContextVariable().getNonNull(execution);

        HttpStatus status;
        try {
            status = HttpStatus.valueOf(httpCode);
        } catch (Exception e) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        AsyncResponse<T> asyncResponse = new AsyncResponse<>(
            status,
            restResponseConnector.getErrorBody(execution));
        execution.setVariable(abstractEventContext.getCallId(), asyncResponse);
    }
}
