package ru.bizweaver.base.metric.delegate;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;

public interface MetricDelegate {

    void increment(String suffix, String metricDescription);

    default void increment(String suffix) {
        increment(suffix, StringUtils.EMPTY);
    }

    /**
     *  execution используется для логирования через аспекты
     */
    default void increment(DelegateExecution execution, String suffix) {
        increment(suffix, StringUtils.EMPTY);
    }
}
