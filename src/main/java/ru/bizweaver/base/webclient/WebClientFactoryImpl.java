package ru.bizweaver.base.webclient;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.spring.webflux.LogbookExchangeFilterFunction;
import reactor.netty.http.client.HttpClient;
import ru.bizweaver.base.webclient.exception.WebClientSslException;
import ru.bizweaver.base.webclient.model.WebClientFactorySettings;
import ru.bizweaver.base.webclient.model.WebClientFactorySettings.DefaultSettings;
import ru.bizweaver.base.webclient.model.WebClientFactorySettings.Point;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class WebClientFactoryImpl implements WebClientFactory {

    private static final String NOT_DEFINED_MESSAGE = "%s must be defined";

    private final WebClientFactorySettings webClientFactorySettings;
    private final Logbook logbook;
    private final Builder builder;

    private final Map<String, Consumer<Builder>> authMap = new HashMap<>();

    @Override
    public WebClient getClient(String serviceName) {
        Point point = getPoint(serviceName);

        // Configure HTTP client with timeouts and SSL
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(point.getReadTimeout()))
                .secure(sslSpec -> sslSpec.sslContext(createSslContext(point.getSsl())));

        // Configure WebClient
        Builder customBuilder = builder.clone()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(point.getUri())
                .filters(filters -> {
                    filters.add(logbookFilter()); // Добавляем Logbook фильтр
                    filters.add(mdcContextFilter());
                });

        // Apply authentication if needed
        Optional.ofNullable(point.getAuth())
                .map(authMap::get)
                .ifPresent(customBuilder::apply);

        return customBuilder.build();
    }

    @Override
    public Point getPoint(String serviceName) {
        Point point = webClientFactorySettings.getPoints().get(serviceName);
        prepareIntegrationSettings(point);
        return point;
    }

    @Override
    public WebClientFactory addAuth(String authName, Consumer<WebClient.Builder> builderConsumer) {
        authMap.put(authName, builderConsumer);
        return this;
    }

    private void prepareIntegrationSettings(Point point) {
        if (Objects.isNull(point.getUri())) {
            throw new IllegalArgumentException(
                    String.format(NOT_DEFINED_MESSAGE, Point.Fields.uri)
            );
        }

        DefaultSettings defaults = webClientFactorySettings.getDefaultSettings();

        if (Objects.isNull(point.getReadTimeout())) {
            point.setReadTimeout(Optional.ofNullable(defaults.getReadTimeout())
                    .orElseThrow(() -> new IllegalArgumentException(
                            String.format(NOT_DEFINED_MESSAGE, DefaultSettings.Fields.readTimeout)
                    )));
        }

        if (Objects.isNull(point.getConnectionTimeout())) {
            point.setConnectionTimeout(Optional.ofNullable(defaults.getConnectionTimeout())
                    .orElseThrow(() -> new IllegalArgumentException(
                            String.format(NOT_DEFINED_MESSAGE, DefaultSettings.Fields.connectionTimeout)
                    )));
        }
    }

    private SslContext createSslContext(WebClientFactorySettings.SslSettings sslSettings) {
        return Optional.ofNullable(sslSettings)
                .filter(WebClientFactorySettings.SslSettings::getEnabled)
                .map(this::buildSecureSslContext)
                .orElseGet(this::buildInsecureSslContext);
    }

    private SslContext buildSecureSslContext(WebClientFactorySettings.SslSettings sslSettings) {
        try {
            return SslContextBuilder.forClient()
                    .protocols(sslSettings.getProtocol())
                    .keyManager(getKeyManagerFactory(sslSettings))
                    .trustManager(getTrustManagerFactory(sslSettings))
                    .build();
        } catch (Exception e) {
            throw new WebClientSslException(e);
        }
    }

    private SslContext buildInsecureSslContext() {
        try {
            return SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
        } catch (SSLException e) {
            throw new WebClientSslException(e);
        }
    }

    private KeyManagerFactory getKeyManagerFactory(WebClientFactorySettings.SslSettings sslSettings)
            throws IOException, KeyStoreException, CertificateException,
            NoSuchAlgorithmException, UnrecoverableKeyException {

        try (FileInputStream fis = new FileInputStream(ResourceUtils.getFile(sslSettings.getKeyStorePath()))) {
            KeyStore keyStore = KeyStore.getInstance(sslSettings.getKeyStoreType());
            keyStore.load(fis, sslSettings.getKeyStorePassword().toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(sslSettings.getAlgorithm());
            kmf.init(keyStore, sslSettings.getKeyStorePassword().toCharArray());
            return kmf;
        }
    }

    private TrustManagerFactory getTrustManagerFactory(WebClientFactorySettings.SslSettings sslSettings)
            throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {

        try (FileInputStream fis = new FileInputStream(ResourceUtils.getFile(sslSettings.getTrustStorePath()))) {
            KeyStore trustStore = KeyStore.getInstance(sslSettings.getTrustStoreType());
            trustStore.load(fis, sslSettings.getTrustStorePassword().toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(sslSettings.getAlgorithm());
            tmf.init(trustStore);
            return tmf;
        }
    }

    private ExchangeFilterFunction mdcContextFilter() {
        return (request, next) -> {
            Map<String, String> mdcContext = MDC.getCopyOfContextMap();
            ClientRequest filteredRequest = ClientRequest.from(request)
                    .headers(headers -> {
                        if (mdcContext != null) {
                            mdcContext.forEach((key, value) ->
                                    headers.add("X-MDC-" + key, value));
                        }
                    })
                    .build();
            return next.exchange(filteredRequest);
        };
    }

    private ExchangeFilterFunction logbookFilter() {
        return new LogbookExchangeFilterFunction(logbook);
    }
}