package ru.bizweaver.base.process.delegate;

import ru.bizweaver.base.metric.delegate.MetricDelegate;
import ru.bizweaver.base.process.connector.EntityConnector;
import ru.bizweaver.base.process.model.ProcessSettings.EntityIntegrationProperties;
import ru.bizweaver.base.process.service.ProcessService;
import ru.bizweaver.base.restintegration.RestIntegrationDelegateFactory;
import ru.bizweaver.base.restintegration.connectors.RestIntegrationInConnector;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityManagerDelegateImpl<T> implements EntityManagerDelegate<T> {

    private static final String ID = "id";
    private static final String LOAD_CASE_NAME = "load";
    private static final String RELOAD_CASE_NAME = "reload";
    private static final String SAVE_CASE_NAME = "save";

    private final EntityIntegrationProperties properties;
    private final RestIntegrationDelegateFactory integrationFactory;
    private final Class<T> clazz;
    private final EntityConnector<T> entityConnector;
    private final MetricDelegate metricDelegate;

    public EntityManagerDelegateImpl(ProcessService<?> processService,
        RestIntegrationDelegateFactory integrationFactory,
        EntityConnector<T> entityConnector,
        Class<T> clazz) {
        this.properties = processService.getEntityIntegrationSettings();
        this.integrationFactory = integrationFactory;
        this.clazz = clazz;
        this.entityConnector = entityConnector;
        this.metricDelegate = processService.getMetricDelegate();
    }


    @Override
    public void load(DelegateExecution execution) {
        if (entityConnector.getEntity(execution) == null) {
            reload(execution, LOAD_CASE_NAME);
        }
    }

    @Override
    public void reload(DelegateExecution execution) {
        reload(execution, RELOAD_CASE_NAME);
    }

    @Override
    public void save(DelegateExecution execution) {
        integrationFactory.delegate(
                SAVE_CASE_NAME,
                properties.getIntegration(),
                properties.getSet(),
                new RestIntegrationInConnector<T>() {
                    @Override
                    public T getBody(DelegateExecution execution) {
                        return entityConnector.getEntity(execution);
                    }

                    @Override
                    public Map<String, ?> getUriParams(DelegateExecution execution) {
                        Map<String, String> uriParams = new HashMap<>();
                        uriParams.put(ID, entityConnector.getBusinessKey(execution));

                        return uriParams;
                    }
                },
                metricDelegate)
            .addSuccessOutConnector(200, clazz, entityConnector::setEntity)
            .execute(execution);
    }

    private void reload(DelegateExecution execution, String caseName) {
        integrationFactory.delegate(
                caseName,
                properties.getIntegration(),
                properties.getGet(),
                new RestIntegrationInConnector<T>() {
                    @Override
                    public Map<String, ?> getUriParams(DelegateExecution execution) {
                        Map<String, String> uriParams = new HashMap<>();
                        uriParams.put(ID, entityConnector.getBusinessKey(execution));

                        return uriParams;
                    }
                },
                metricDelegate)
            .addSuccessOutConnector(200, clazz, entityConnector::setEntity)
            .execute(execution);
    }
}
