package ru.bizweaver.base.webclient.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.util.MultiValueMap;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebClientFactorySettings {

    private DefaultSettings defaultSettings;
    private Map<String, Point> points;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldNameConstants
    public static class DefaultSettings {
        private Long readTimeout;
        private Long connectionTimeout;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldNameConstants
    public static class Point {
        private String uri;
        private MultiValueMap<String, String> headers;
        private String auth;
        private Long readTimeout;
        private Long connectionTimeout;
        private SslSettings ssl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SslSettings {
        private Boolean enabled;
        private String protocol;
        private String algorithm;
        private Boolean insecure;
        private String keyStorePassword;
        private String keyStorePath;
        private String keyStoreType;
        private String trustStorePassword;
        private String trustStorePath;
        private String trustStoreType;
    }
}
