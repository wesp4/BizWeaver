package ru.bizweaver.base.featuretoggles;

import ru.bizweaver.base.featuretoggles.model.FeatureTogglesSettings;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

@RequiredArgsConstructor
public class FeatureTogglesDelegate<T> implements JavaDelegate {

    private final FeatureTogglesSettings<T> settings;
    private final FeatureTogglesConnector<T> connector;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        final String key = connector.getFeatureKey(execution);
        final T defaultGroup = settings.getFtMap().get(settings.getDefaultGroup());
        connector.setFeatureToggles(execution, settings.getFtMap().getOrDefault(key, defaultGroup));
    }
}
