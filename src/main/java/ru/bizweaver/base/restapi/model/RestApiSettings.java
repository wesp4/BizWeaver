package ru.bizweaver.base.restapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestApiSettings {

    private Map<String, ApiSettings> restApiSettings;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApiSettings {
        private Long responseTimeoutInMilliSeconds;
        private Long numberOfAttempts;
        private Long waitingTime;
    }
}
