package ru.bizweaver.base.webclient;


import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import ru.bizweaver.base.webclient.model.WebClientFactorySettings;

import java.util.function.Consumer;

public interface WebClientFactory {

    WebClient getClient(String serviceName);

    WebClientFactorySettings.Point getPoint(String serviceName);

    WebClientFactory addAuth(String authName, Consumer<Builder> builderConsumer);
}
