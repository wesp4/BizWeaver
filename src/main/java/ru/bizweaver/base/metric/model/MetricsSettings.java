package ru.bizweaver.base.metric.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricsSettings {

    private String notDefinedMessage;
    private String defaultGroup;
    private Map<String, MetricValue> groupMetrics;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MetricValue {
        private String prefix;
        private String pattern;
    }
}