package ru.bizweaver.base.restintegration.connectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public interface RestIntegrationInConnector<T> {

    default Map<String, ?> getUriParams(DelegateExecution execution) {
        return new HashMap<>();
    }

    default MultiValueMap<String, String> getQueryParams(DelegateExecution execution) {
        return new LinkedMultiValueMap<>();
    }

    default Consumer<HttpHeaders> getHeaders(DelegateExecution execution) {
        return httpHeaders -> {
        };
    }

    default T getBody(DelegateExecution execution) {
        return null;
    }
}
