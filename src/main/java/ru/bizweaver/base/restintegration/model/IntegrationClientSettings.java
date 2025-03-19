package ru.bizweaver.base.restintegration.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.util.MultiValueMap;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationClientSettings {

    private DefaultSettings defaultSettings;
    private Map<String, RestClientSettings> services;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldNameConstants
    public static class DefaultSettings {
        private Long initialInterval;
        private Long retries;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldNameConstants
    public static class RestClientSettings {
        private String webClient;
        private String path;
        private MultiValueMap<String, String> headers;
        private Map<String, MethodSettings> methods;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldNameConstants
    public static class MethodSettings {
        private Long initialInterval;
        private Long retries;
        private String path;
        private String method;
        private MultiValueMap<String, String> headers;
        private Integer size;
        private Map<String, String> metrics;
    }
}
