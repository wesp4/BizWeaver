package ru.bizweaver.base.process.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessSettings<P extends AbstractProcessContext> {

    private String masterProcessKey;
    private P context;
    private EntityIntegrationProperties entity;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EntityIntegrationProperties {
        private String integration;
        private String get;
        private String set;
    }
}
