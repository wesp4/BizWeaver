package ru.bizweaver.base.process.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;

public interface EntityManagerDelegate<T> {
    void load(DelegateExecution execution);
    void reload(DelegateExecution execution);
    void save(DelegateExecution execution);
}
