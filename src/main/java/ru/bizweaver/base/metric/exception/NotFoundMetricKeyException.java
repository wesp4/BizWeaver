package ru.bizweaver.base.metric.exception;

public class NotFoundMetricKeyException extends RuntimeException {

    private static final String MESSAGE = "Not found metric key: %s";

    public NotFoundMetricKeyException(String metricKey) {
        super(MESSAGE.formatted(metricKey));
    }
}
