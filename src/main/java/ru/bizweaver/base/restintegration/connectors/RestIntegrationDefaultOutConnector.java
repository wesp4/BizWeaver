package ru.bizweaver.base.restintegration.connectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;

public interface RestIntegrationDefaultOutConnector {
    default void execute(DelegateExecution execution) {
    }
}
