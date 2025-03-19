package ru.bizweaver.base.restapi.service;

import ru.bizweaver.base.process.model.AbstractEventContext;
import ru.bizweaver.base.restapi.model.AsyncResponse;
import ru.bizweaver.base.process.service.ProcessService;

import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface RestApiService<T> {

    Mono<ResponseEntity<T>> createSyncResponse(AbstractEventContext eventContext);

    Mono<ResponseEntity<T>> createAsyncResponse(AbstractEventContext eventContext);

    Mono<ResponseEntity<T>> checkResponse(String businessKey, String callId);

    void setResponse(AbstractEventContext eventContext, AsyncResponse<T> asyncResponse);

    ProcessService<?> getProcessService();
}
