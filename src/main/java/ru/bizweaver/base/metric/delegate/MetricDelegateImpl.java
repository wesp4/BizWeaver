package ru.bizweaver.base.metric.delegate;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RequiredArgsConstructor
public class MetricDelegateImpl implements MetricDelegate {

    private final String metricPrefix;
    private final String metricNamePattern;
    private final MeterRegistry registry;
    private final ConcurrentMap<String, Counter> metricCounters = new ConcurrentHashMap<>();


    @Override
    public void increment(String suffix, String metricDescription) {
        if (suffix != null) {
            if (!metricCounters.containsKey(suffix)) {
                metricCounters.put(suffix, Counter
                    .builder(metricNamePattern.formatted(metricPrefix, suffix))
                    .description(metricDescription)
                    .register(registry));
            }
            metricCounters.get(suffix).increment();
        }
    }
}
