package ru.bizweaver.base.restapi.service;

import jakarta.validation.constraints.NotNull;
import ru.bizweaver.base.process.model.AbstractEventContext;
import ru.bizweaver.base.process.service.ProcessService;
import ru.bizweaver.base.restapi.exception.AsyncResponseNotFoundException;
import ru.bizweaver.base.restapi.exception.ProcessHasErrorsException;
import ru.bizweaver.base.restapi.exception.RequestTimeoutException;
import ru.bizweaver.base.restapi.exception.UseAsyncException;
import ru.bizweaver.base.restapi.model.AsyncResponse;
import ru.bizweaver.base.restapi.service.model.CheckAcceptedData;
import ru.bizweaver.base.restapi.variable.AsyncResponseVariable;
import ru.bizweaver.base.utils.ProcessUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class RestApiServiceImpl<T> implements RestApiService<T> {

    private static final int RETRY_MAX_ATTEMPTS = 3;
    private static final int RETRY_DELAY = 500;
    private static final double RETRY_JITTER = 0d;

    private final String correlationKey;
    private final Long responseTimeoutInMilliSeconds;
    private final Long numberOfAttempts;
    private final Long waitingTime;
    private final ProcessService<?> processService;


    @Override
    public Mono<ResponseEntity<T>> createSyncResponse(AbstractEventContext eventContext) {
        return createResponse(eventContext)
            .onErrorResume(UseAsyncException.class, ex -> {
                String callId = eventContext.getCallId();
                return asyncParseResponse(eventContext.getBusinessKey(), callId)
                    .retryWhen(Retry.fixedDelay(numberOfAttempts, Duration.ofMillis(waitingTime))
                        .jitter(RETRY_JITTER)
                        .filter(throwable -> throwable instanceof UseAsyncException)
                        .onRetryExhaustedThrow(((retryBackoffSpec, retrySignal) -> {
                            throw new RequestTimeoutException();
                        }))
                        .doBeforeRetry(retrySignal -> log.trace(
                            String.format(
                                "Проверка наличия асинхронного ответа с callId: %s и businessKey: %s",
                                callId, eventContext.getBusinessKey()
                            ),
                            retrySignal.totalRetries() + 1)
                        ));
            })
            .timeout(
                Duration.ofMillis(responseTimeoutInMilliSeconds),
                Mono.error(new RequestTimeoutException())
            );
    }

    @Override
    public Mono<ResponseEntity<T>> createAsyncResponse(AbstractEventContext eventContext) {
        return createResponse(eventContext)
            .timeout(
                Duration.ofMillis(responseTimeoutInMilliSeconds),
                Mono.error(new RequestTimeoutException())
            );
    }

    @Override
    public Mono<ResponseEntity<T>> checkResponse(String businessKey, String callId) {
        final String logIds = ProcessUtils.getLogIds(businessKey, callId);

        return asyncParseResponse(businessKey, callId)
            .onErrorMap(this::isExceptionRetryable, exc -> {
                log.error("{} Не удалось получить асинхронный ответ для запроса {}, ошибка: {}",
                    logIds, callId, exc.getMessage());
                return exc;
            })
            .timeout(Duration.ofMillis(responseTimeoutInMilliSeconds),
                Mono.error(new RequestTimeoutException()));
    }

    @Override
    public void setResponse(AbstractEventContext
        eventContext, AsyncResponse<T> asyncResponse) {
        String variableName = AsyncResponseVariable.generateVariableName(
            eventContext.getCallId());
        processService.setVariableByName(variableName, eventContext.getBusinessKey(),
            asyncResponse);
    }

    @Override
    public ProcessService<?> getProcessService() {
        return processService;
    }

    @NotNull
    private Mono<ResponseEntity<T>> createResponse(AbstractEventContext eventContext) {
        String variableName = AsyncResponseVariable.generateVariableName(
            eventContext.getCallId());
        Map<String, Object> variables = Map.of(
            variableName,
            new AsyncResponse<T>(HttpStatus.ACCEPTED)
        );
        return Mono.create((MonoSink<ResponseEntity<T>> sink) -> {
            ProcessUtils.updateContext(eventContext.getMdcContext());
            try {
                processService.sendExternalEvent(correlationKey, eventContext, variables);
                sink.error(new UseAsyncException(
                    new CheckAcceptedData(eventContext.getBusinessKey(),
                        eventContext.getCallId()))
                );
            } catch (Exception ex) {
                sink.error(ex);
            }
        });
    }

    private ResponseEntity<T> parseResponse(String businessKey, String callId) {
        String variableName = AsyncResponseVariable.generateVariableName(callId);
        AsyncResponse<T> asyncResponse = processService.getVariableByName(variableName,
            businessKey);
        if (asyncResponse == null) {
            throw new AsyncResponseNotFoundException();
        } else if (asyncResponse.getStatus() == HttpStatus.OK) {
            return new ResponseEntity<>(asyncResponse.getSuccessResponse(), HttpStatus.OK);
        } else if (asyncResponse.getStatus() == HttpStatus.ACCEPTED) {
            throw new UseAsyncException(
                new CheckAcceptedData(businessKey, callId));
        } else {
            throw new ProcessHasErrorsException(asyncResponse.getStatus(),
                asyncResponse.getErrors());
        }
    }

    private Mono<ResponseEntity<T>> asyncParseResponse(String businessKey, String callId) {
        return Mono.create((MonoSink<ResponseEntity<T>> sink) -> {
            String variableName = AsyncResponseVariable.generateVariableName(callId);
            AsyncResponse<T> asyncResponse = null;
            try {
                asyncResponse = processService.getVariableByName(variableName, businessKey);
            } catch (Exception exception) {
                sink.error(exception);
            }

            if (asyncResponse == null) {
                sink.error(new AsyncResponseNotFoundException());
            } else if (asyncResponse.getStatus() == HttpStatus.OK) {
                sink.success(
                    new ResponseEntity<>(asyncResponse.getSuccessResponse(), HttpStatus.OK));
            } else if (asyncResponse.getStatus() == HttpStatus.ACCEPTED) {
                sink.error(new UseAsyncException(
                    new CheckAcceptedData(businessKey, callId)));
            } else {
                sink.error(new ProcessHasErrorsException(asyncResponse.getStatus(),
                    asyncResponse.getErrors()));
            }
        });
//            .retryWhen(buildRetryPolicy());

    }

    private Retry buildRetryPolicy() {
        return Retry.backoff(RETRY_MAX_ATTEMPTS, Duration.ofMillis(RETRY_DELAY))
            .jitter(RETRY_JITTER)
            .filter(this::isExceptionRetryable)
            .onRetryExhaustedThrow((spec, signal) -> signal.failure());
    }

    private boolean isExceptionRetryable(Throwable t) {
        return !(t instanceof AsyncResponseNotFoundException
            || t instanceof UseAsyncException
            || t instanceof ProcessHasErrorsException);
    }
}
