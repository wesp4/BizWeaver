package ru.bizweaver.base.process.connector;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

public interface EntityConnector<T> {

    String getBusinessKey(DelegateExecution execution);

    void setEntity(DelegateExecution execution, T entity);

    T getEntity(DelegateExecution execution);
}
