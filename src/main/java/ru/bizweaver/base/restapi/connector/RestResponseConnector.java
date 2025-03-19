package ru.bizweaver.base.restapi.connector;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import ru.bizweaver.base.restapi.model.ErrorContainer;

import java.util.Collections;
import java.util.List;

public interface RestResponseConnector<T> {

    default T getSuccessBody(DelegateExecution execution) {
        return null;
    }

    default List<ErrorContainer> getErrorBody(DelegateExecution execution) {
        return Collections.emptyList();
    }
}
