package ru.bizweaver.base.restintegration;

import ru.bizweaver.base.metric.delegate.MetricDelegate;
import ru.bizweaver.base.restintegration.connectors.RestIntegrationDefaultOutConnector;
import ru.bizweaver.base.restintegration.connectors.RestIntegrationInConnector;
import ru.bizweaver.base.restintegration.delegate.RestIntegrationDelegate;
import ru.bizweaver.base.restintegration.delegate.RestIntegrationDelegateImpl;
import ru.bizweaver.base.restintegration.exception.NotFoundSettingsException;
import ru.bizweaver.base.restintegration.model.IntegrationClientSettings;
import ru.bizweaver.base.restintegration.model.IntegrationClientSettings.DefaultSettings;
import ru.bizweaver.base.restintegration.model.IntegrationClientSettings.MethodSettings;
import ru.bizweaver.base.restintegration.model.IntegrationClientSettings.RestClientSettings;
import ru.bizweaver.base.webclient.WebClientFactory;

import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
public class RestIntegrationDelegateFactoryImpl implements RestIntegrationDelegateFactory {

    private static final String NOT_DEFINED_MESSAGE = "%s must be defined";
    private static final String EXCEPTION_CODE_PATTERN = "%s_%s_%s";

    private final WebClientFactory webClientFactory;
    private final IntegrationClientSettings integrationClientSettings;

    @Override
    public <T> RestIntegrationDelegate<T> delegate(
        String caseName,
        String serviceName,
        String methodName,
        RestIntegrationInConnector<T> inConnector,
        MetricDelegate metricDelegate,
        RestIntegrationDefaultOutConnector defaultOutConnector
    ) {
        RestClientSettings restClientSettings = Optional.ofNullable(
                integrationClientSettings.getServices())
            .map(service -> service.get(serviceName))
            .orElseThrow(() -> new NotFoundSettingsException(serviceName));
        MethodSettings methodSettings = restClientSettings.getMethods().get(methodName);
        prepareIntegrationSettings(restClientSettings, methodSettings);
        String exceptionCode = EXCEPTION_CODE_PATTERN.formatted(caseName, methodName,
                serviceName).toUpperCase();
        return new RestIntegrationDelegateImpl<>(
            webClientFactory.getClient(restClientSettings.getWebClient()),
            webClientFactory.getPoint(restClientSettings.getWebClient()),
            restClientSettings,
            methodSettings,
            inConnector,
            defaultOutConnector,
            metricDelegate,
            exceptionCode
        );
    }

    @Override
    public <T> RestIntegrationDelegate<T> delegate(
        String caseName,
        String serviceName,
        String methodName,
        RestIntegrationInConnector<T> inConnector,
        MetricDelegate metricDelegate
    ) {
        return delegate(caseName, serviceName, methodName, inConnector, metricDelegate, null);
    }

    private void prepareIntegrationSettings(
        RestClientSettings restClientSettings,
        MethodSettings methodSettings
    ) {
        if (Objects.isNull(restClientSettings.getPath())) {
            restClientSettings.setPath("");
        }
        if (Objects.isNull(restClientSettings.getWebClient())) {
            throw new IllegalArgumentException(
                String.format(RestIntegrationDelegateFactoryImpl.NOT_DEFINED_MESSAGE,
                    RestClientSettings.Fields.webClient)
            );
        }
        if (Objects.isNull(methodSettings)) {
            throw new IllegalArgumentException(
                String.format(RestIntegrationDelegateFactoryImpl.NOT_DEFINED_MESSAGE,
                    RestClientSettings.Fields.methods)
            );
        }
        if (Objects.isNull(methodSettings.getPath())) {
            methodSettings.setPath("");
        }
        if (Objects.isNull(methodSettings.getMethod())) {
            throw new IllegalArgumentException(
                String.format(RestIntegrationDelegateFactoryImpl.NOT_DEFINED_MESSAGE,
                    MethodSettings.Fields.method)
            );
        }
        if (Objects.isNull(methodSettings.getInitialInterval())) {
            methodSettings.setInitialInterval((Optional.ofNullable(
                    integrationClientSettings.getDefaultSettings().getInitialInterval())
                .orElseThrow(() -> new IllegalArgumentException(
                    String.format(RestIntegrationDelegateFactoryImpl.NOT_DEFINED_MESSAGE,
                        DefaultSettings.Fields.initialInterval)
                ))));
        }
        if (Objects.isNull(methodSettings.getRetries())) {
            methodSettings.setRetries(Optional.ofNullable(
                    integrationClientSettings.getDefaultSettings().getRetries())
                .orElseThrow(() -> new IllegalArgumentException(
                    String.format(RestIntegrationDelegateFactoryImpl.NOT_DEFINED_MESSAGE,
                        DefaultSettings.Fields.retries)
                ))
            );
        }
    }

}

