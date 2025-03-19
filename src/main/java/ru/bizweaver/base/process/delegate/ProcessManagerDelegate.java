package ru.bizweaver.base.process.delegate;

import ru.bizweaver.base.process.exception.EventCorrelationException;

import org.camunda.bpm.engine.delegate.DelegateExecution;

public interface ProcessManagerDelegate<T> {

    void scheduleAsyncProcess(DelegateExecution execution, String processName);

    void finishAsyncProcessById(DelegateExecution execution);

    void finishAsyncProcess(DelegateExecution execution);

    void sendFinishAsyncProcess(DelegateExecution execution, String finishEventName) throws EventCorrelationException;

    void sendFinishAsyncProcessWithRestResponse(DelegateExecution execution, String finishEventName) throws EventCorrelationException;

    Boolean hasAnyAsyncProcess(DelegateExecution execution);

    Boolean eventEquals(DelegateExecution execution, String eventName);

    Boolean useActiveStory(DelegateExecution execution, String story);
}
