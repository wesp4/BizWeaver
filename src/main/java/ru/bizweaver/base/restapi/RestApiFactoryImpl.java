package ru.bizweaver.base.restapi;

import ru.bizweaver.base.process.service.ProcessService;
import ru.bizweaver.base.restapi.model.RestApiSettings;
import ru.bizweaver.base.restapi.model.RestApiSettings.ApiSettings;
import ru.bizweaver.base.restapi.service.RestApiService;
import ru.bizweaver.base.restapi.service.RestApiServiceImpl;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RestApiFactoryImpl implements RestApiFactory {

    private final RestApiSettings restApiSettings;

    @Override
    public <T> RestApiService<T> service(String correlationKey,
                                         String apiName,
                                         ProcessService<?> processService) {
       ApiSettings apiSettings = restApiSettings.getRestApiSettings().get(apiName);

        return new RestApiServiceImpl<>(
            correlationKey,
            apiSettings.getResponseTimeoutInMilliSeconds(),
            apiSettings.getNumberOfAttempts(),
            apiSettings.getWaitingTime(),
            processService
            );
    }
}
