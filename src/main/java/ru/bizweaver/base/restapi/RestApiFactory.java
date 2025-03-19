package ru.bizweaver.base.restapi;

import ru.bizweaver.base.process.service.ProcessService;
import ru.bizweaver.base.restapi.service.RestApiService;

public interface RestApiFactory {
    <T> RestApiService<T> service(String correlationKey,
                                  String apiName,
                                  ProcessService<?> processService);
}
