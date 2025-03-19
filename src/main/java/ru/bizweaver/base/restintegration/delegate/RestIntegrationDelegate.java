package ru.bizweaver.base.restintegration.delegate;

import ru.bizweaver.base.restintegration.connectors.RestIntegrationResponseOutConnector;
import ru.bizweaver.base.restintegration.model.RestIntegrationMetricType;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public interface RestIntegrationDelegate<T> extends JavaDelegate {

    void execute(DelegateExecution execution);


    <R> RestIntegrationDelegate<T> addSuccessOutConnector(int status, Class<R> clazz,
        RestIntegrationResponseOutConnector<R> connector);

    <R> RestIntegrationDelegate<T> addSuccessOutConnector(int status, Class<R> clazz);

    <R> RestIntegrationDelegate<T> addErrorOutConnector(int status, Class<R> clazz,
        RestIntegrationResponseOutConnector<R> connector);

    RestIntegrationDelegate<T> addMetric(RestIntegrationMetricType type, String suffix);
}
