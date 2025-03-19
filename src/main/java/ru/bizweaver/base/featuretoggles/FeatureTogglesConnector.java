package ru.bizweaver.base.featuretoggles;

import org.camunda.bpm.engine.delegate.DelegateExecution;

public interface FeatureTogglesConnector<T> {

    String getFeatureKey(DelegateExecution execution);

    void setFeatureToggles(DelegateExecution execution, T toggles);
}
