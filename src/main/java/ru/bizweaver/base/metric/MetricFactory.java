package ru.bizweaver.base.metric;

import ru.bizweaver.base.metric.delegate.MetricDelegate;
import ru.bizweaver.base.metric.delegate.MetricDelegateImpl;
import ru.bizweaver.base.metric.exception.NotFoundMetricKeyException;
import ru.bizweaver.base.metric.model.MetricsSettings;
import ru.bizweaver.base.metric.model.MetricsSettings.MetricValue;
import ru.bizweaver.base.restintegration.model.IntegrationClientSettings.RestClientSettings;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
public class MetricFactory {

    private final MeterRegistry metricRegistry;
    private final MetricsSettings metricsSettings;

    public MetricDelegate delegate(String metricGroup) {
        MetricValue metricValue = Optional.ofNullable(
            metricsSettings.getGroupMetrics().get(metricGroup)).orElseThrow(
            () -> new NotFoundMetricKeyException(metricGroup)
        );
        MetricValue defaltMetricValue = Optional.ofNullable(
            metricsSettings.getGroupMetrics().get(metricsSettings.getDefaultGroup())).orElseThrow(
            () -> new NotFoundMetricKeyException(metricsSettings.getDefaultGroup())
        );

        prepareMetricSettings(metricValue, defaltMetricValue);
        return new MetricDelegateImpl(metricValue.getPrefix(), metricValue.getPattern(), metricRegistry);
    }

    private void prepareMetricSettings(
        MetricValue metricValue,
        MetricValue defaultMetricValue
    ) {
        if (Objects.isNull(metricValue.getPrefix())) {
            throw new IllegalArgumentException(
                String.format(metricsSettings.getNotDefinedMessage(), RestClientSettings.Fields.webClient)
            );
        }
        if (Objects.isNull(metricValue.getPattern())) {
            metricValue.setPattern(defaultMetricValue.getPattern());
        }
    }
}