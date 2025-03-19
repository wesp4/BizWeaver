package ru.bizweaver.base.restintegration.delegate;


import ru.bizweaver.base.metric.delegate.MetricDelegate;
import ru.bizweaver.base.restintegration.connectors.RestIntegrationDefaultOutConnector;
import ru.bizweaver.base.restintegration.connectors.RestIntegrationInConnector;
import ru.bizweaver.base.restintegration.connectors.RestIntegrationResponseOutConnector;
import ru.bizweaver.base.restintegration.exception.DefaultOutConnectorException;
import ru.bizweaver.base.restintegration.exception.InConnectorException;
import ru.bizweaver.base.restintegration.exception.NotAvailableIntegrationException;
import ru.bizweaver.base.restintegration.exception.NotFoundOutConnectorException;
import ru.bizweaver.base.restintegration.exception.OutConnectorException;
import ru.bizweaver.base.restintegration.exception.SetErrorResultException;
import ru.bizweaver.base.restintegration.exception.UnknownIntegrationException;
import ru.bizweaver.base.restintegration.model.IntegrationClientSettings.MethodSettings;
import ru.bizweaver.base.restintegration.model.IntegrationClientSettings.RestClientSettings;
import ru.bizweaver.base.restintegration.model.RestIntegrationMetricType;
import ru.bizweaver.base.webclient.model.WebClientFactorySettings.Point;

import lombok.Getter;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

public class RestIntegrationDelegateImpl<T> implements RestIntegrationDelegate<T> {

    private static final double RETRY_JITTER = 0d;

    private final String uri;
    private final HttpMethod method;
    private final Long initialInterval;
    private final Long retries;
    private final WebClient webClient;
    private final RestIntegrationInConnector<T> inConnector;
    private final RestIntegrationDefaultOutConnector defaultOutConnector;
    private final String exceptionCode;
    private final MetricDelegate metricDelegate;
    private final MethodSettings methodSettings;
    private final Map<Integer, RestIntegrationHandler<?>> outConnectorMap = new HashMap<>();
    private final Map<RestIntegrationMetricType, String> metricTypeMap = new HashMap<>();

    public RestIntegrationDelegateImpl(
        WebClient webClient,
        Point point,
        RestClientSettings restClientSettings,
        MethodSettings methodSettings,
        RestIntegrationInConnector<T> inConnector,
        RestIntegrationDefaultOutConnector defaultOutConnector,
        MetricDelegate metricDelegate,
        String exceptionCode
    ) {
        this.inConnector = inConnector;
        this.defaultOutConnector = defaultOutConnector;

        final ExchangeStrategies strategies = ExchangeStrategies.builder()
            .codecs(codecs -> {
                    if (Objects.nonNull(methodSettings.getSize())) {
                        codecs.defaultCodecs().maxInMemorySize(methodSettings.getSize());
                    }
                }
            )
            .build();

        this.webClient = webClient.mutate()
            .exchangeStrategies(strategies)
            .defaultHeaders(httpHeaders -> {
                Optional.ofNullable(restClientSettings.getHeaders())
                    .ifPresent(httpHeaders::addAll);
                Optional.ofNullable(point.getHeaders())
                    .ifPresent(httpHeaders::addAll);
            })
            .build();

        this.uri = restClientSettings.getPath().concat(methodSettings.getPath());

        this.method = HttpMethod.valueOf(methodSettings.getMethod().toUpperCase());

        this.initialInterval = methodSettings.getInitialInterval();
        this.retries = methodSettings.getRetries();

        if (Objects.nonNull(methodSettings.getMetrics())) {
            for (Entry<String, String> stringStringEntry : methodSettings.getMetrics().entrySet()) {
                metricTypeMap.put(
                    RestIntegrationMetricType.getByName(stringStringEntry.getKey()),
                    stringStringEntry.getValue()
                );
            }
        }

        this.metricDelegate = metricDelegate;
        this.exceptionCode = exceptionCode;
        this.methodSettings = methodSettings;
    }

    @Override
    public void execute(DelegateExecution execution) {
        sendMetric(RestIntegrationMetricType.START);
        try {
            RestIntegrationHandler<?> restIntegrationHandler =
                Mono.create((MonoSink<RequestBodySpec> sink) -> {
                        try {
                            sink.success(buildWebclient(execution));
                        } catch (Throwable ex) {
                            sink.error(ex);
                        }
                    })
                    .map(requestBodySpec -> {
                        if (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT)) {
                            return requestBodySpec.bodyValue(inConnector.getBody(execution));
                        }
                        return requestBodySpec;
                    })
                    .onErrorMap(ex -> {
                        if (ex instanceof BpmnError) {
                            return ex;
                        }
                        return new InConnectorException(exceptionCode, ex.getMessage());
                    })
                    .flatMap(requestHeadersSpec -> requestHeadersSpec.exchangeToMono(
                        this::createRestIntegrationHandler)
                    )
                    .retryWhen(buildRetryPolicy())
                    .onErrorMap(ex -> {
                        if (!(ex instanceof BpmnError)) {
                            return new NotAvailableIntegrationException(exceptionCode);
                        }
                        return ex;
                    })
                    .block();

            Optional.ofNullable(restIntegrationHandler)
                .ifPresent(
                    handler -> {
                        if (handler.execute(execution, exceptionCode)) {
                            sendMetric(RestIntegrationMetricType.SUCCESS);
                        } else {
                            throw new SetErrorResultException(exceptionCode);
                        }
                    }
                );
        } catch (SetErrorResultException ex) {
            sendMetric(RestIntegrationMetricType.FAILURE);
            throw ex;
        } catch (BpmnError ex) {
            defaultOutConnectorHandler(execution);
            throw ex;
        } catch (Exception ex) {
            defaultOutConnectorHandler(execution);
            throw new UnknownIntegrationException(exceptionCode, ex);
        }
    }

    @Override
    public <R> RestIntegrationDelegate<T> addSuccessOutConnector(int status, Class<R> clazz,
        RestIntegrationResponseOutConnector<R> connector) {
        RestIntegrationHandler<R> handler = new RestIntegrationHandler<>(clazz, connector,
            Boolean.TRUE);
        outConnectorMap.put(status, handler);
        return this;
    }

    @Override
    public <R> RestIntegrationDelegate<T> addSuccessOutConnector(int status, Class<R> clazz) {
        RestIntegrationHandler<R> handler = new RestIntegrationHandler<>(clazz, Boolean.TRUE);
        outConnectorMap.put(status, handler);
        return this;
    }

    @Override
    public <R> RestIntegrationDelegate<T> addErrorOutConnector(int status, Class<R> clazz,
        RestIntegrationResponseOutConnector<R> connector) {
        RestIntegrationHandler<R> handler = new RestIntegrationHandler<>(clazz, connector,
            Boolean.FALSE);
        outConnectorMap.put(status, handler);
        return this;
    }

    @Override
    public RestIntegrationDelegate<T> addMetric(RestIntegrationMetricType type, String suffix) {
        metricTypeMap.put(type, suffix);
        return this;
    }

    private void sendMetric(RestIntegrationMetricType metricType) {
        Optional.ofNullable(metricTypeMap.get(metricType)).ifPresent(
            metricDelegate::increment
        );
    }

    @Getter
    private static class RestIntegrationHandler<R> {

        private final Class<R> clazz;
        private final Boolean isSuccess;
        private final RestIntegrationResponseOutConnector<R> outConnector;

        private R body;

        public RestIntegrationHandler(Class<R> clazz,
            RestIntegrationResponseOutConnector<R> outConnector,
            Boolean isSuccess) {
            this.clazz = clazz;
            this.outConnector = outConnector;
            this.isSuccess = isSuccess;
        }

        public RestIntegrationHandler(Class<R> clazz,
            Boolean isSuccess) {
            this.clazz = clazz;
            this.outConnector = null;
            this.isSuccess = isSuccess;
        }

        public RestIntegrationHandler<R> body(R body) {
            this.body = body;

            return this;
        }

        public Boolean execute(DelegateExecution execution, String exceptionCode) {
            try {
                Optional.ofNullable(outConnector)
                    .ifPresent(connector -> connector.execute(execution, body));
            } catch (Exception e) {
                throw new OutConnectorException(exceptionCode);
            }
            return isSuccess;
        }
    }

    private RequestBodySpec buildWebclient(DelegateExecution execution) {
        return webClient.method(method)
            .uri(uriBuilder -> uriBuilder
                .path(uri)
                .queryParams(inConnector.getQueryParams(execution))
                .build(inConnector.getUriParams(execution))
            )
            .headers(inConnector.getHeaders(execution)
                .andThen(httpHeaders -> Optional.ofNullable(methodSettings.getHeaders())
                    .ifPresent(httpHeaders::addAll)
                )
            );
    }

    private Mono<? extends RestIntegrationHandler<?>> createRestIntegrationHandler(
        ClientResponse response) {
        final int rawStatusCode = response.statusCode().value();
        final HttpStatus httpStatus = HttpStatus.resolve(rawStatusCode);
        if (Objects.nonNull(httpStatus)) {
            if (HttpStatus.SERVICE_UNAVAILABLE.equals(httpStatus)
                || HttpStatus.BAD_GATEWAY.equals(httpStatus)
                || HttpStatus.GATEWAY_TIMEOUT.equals(httpStatus)) {
                return Mono.error(new NotAvailableIntegrationException(exceptionCode));
            }
            if (HttpStatus.NOT_FOUND.equals(httpStatus)) {
                boolean isCorrectMediaType = response.headers().contentType()
                    .map(type -> type.equals(MediaType.APPLICATION_JSON))
                    .orElse(false);
                if (!isCorrectMediaType) {
                    return Mono.error(
                        new NotAvailableIntegrationException(exceptionCode));
                }
            }
        }

        return Optional.ofNullable(outConnectorMap.get(rawStatusCode))
            .map(handler -> response
                .bodyToMono(handler.getClazz())
                .map(handler::body)
            )
            .orElse(Mono.error(new NotFoundOutConnectorException(exceptionCode)));
    }

    private Retry buildRetryPolicy() {
        return Retry.backoff(initialInterval == 0 ? 0 : retries,
                Duration.ofMillis(initialInterval)
            )
            .jitter(RETRY_JITTER)
            .filter(this::isExceptionRetryable)
            .onRetryExhaustedThrow((spec, signal) -> signal.failure());
    }

    private boolean isExceptionRetryable(Throwable t) {
        return t instanceof NotAvailableIntegrationException
            || t instanceof WebClientRequestException;
    }

    private void defaultOutConnectorHandler(DelegateExecution execution) {
        sendMetric(RestIntegrationMetricType.FAILURE);
        try {
            Optional.ofNullable(defaultOutConnector)
                .ifPresent(connector -> connector.execute(execution));
        } catch (Exception e) {
            throw new DefaultOutConnectorException(exceptionCode);
        }
    }
}
