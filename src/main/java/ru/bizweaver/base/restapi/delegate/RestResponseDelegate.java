package ru.bizweaver.base.restapi.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;

public interface RestResponseDelegate<T> {

    void setSuccess(DelegateExecution execution);

    void setError(DelegateExecution execution, Integer httpCode);

    void saveSuccess(DelegateExecution execution);

    void saveError(DelegateExecution execution, Integer httpCode);
}

