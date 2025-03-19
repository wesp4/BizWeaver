package ru.bizweaver.base.restintegration;

import ru.bizweaver.base.metric.delegate.MetricDelegate;
import ru.bizweaver.base.restintegration.connectors.RestIntegrationDefaultOutConnector;
import ru.bizweaver.base.restintegration.connectors.RestIntegrationInConnector;
import ru.bizweaver.base.restintegration.delegate.RestIntegrationDelegate;

public interface RestIntegrationDelegateFactory {
    <T> RestIntegrationDelegate<T> delegate(
        String caseName,
        String serviceName,
        String methodName,
        RestIntegrationInConnector<T> inConnector,
        MetricDelegate metricDelegate,
        RestIntegrationDefaultOutConnector defaultOutConnector
    );

    <T> RestIntegrationDelegate<T> delegate(
        String caseName,
        String serviceName,
        String methodName,
        RestIntegrationInConnector<T> inConnector,
        MetricDelegate metricDelegate
    );
}
