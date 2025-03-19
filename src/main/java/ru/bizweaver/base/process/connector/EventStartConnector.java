package ru.bizweaver.base.process.connector;

import ru.bizweaver.base.process.model.AbstractEventContext;

import org.camunda.bpm.engine.delegate.DelegateExecution;

import java.util.HashMap;
import java.util.Map;

public interface EventStartConnector<T extends AbstractEventContext> {
    T getContext(DelegateExecution execution);

    default Map<String, Object> getVariables(DelegateExecution execution){
        return new HashMap<>();
    };
}