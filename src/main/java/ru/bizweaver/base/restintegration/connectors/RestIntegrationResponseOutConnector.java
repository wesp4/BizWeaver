package ru.bizweaver.base.restintegration.connectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;

public interface RestIntegrationResponseOutConnector<T> {

    void execute(DelegateExecution execution, T body);
}
