package ru.bizweaver.base.restintegration.model;

import jakarta.validation.constraints.NotNull;
import ru.bizweaver.base.restintegration.exception.NotFoundMetricKeyException;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum RestIntegrationMetricType {

    START("start"),
    SUCCESS("success"),
    FAILURE("failure");

    private final String yamlName;

    RestIntegrationMetricType(@NotNull String yamlName) {
        this.yamlName = yamlName;
    }

    public static RestIntegrationMetricType getByName(String name) {
        return Arrays.stream(RestIntegrationMetricType.values())
            .filter(metricType -> metricType.yamlName.equals(name))
            .findFirst()
            .orElseThrow(() -> new NotFoundMetricKeyException(name));
    }
}
