package ru.bizweaver.base.featuretoggles.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeatureTogglesSettings<T> {

    private String defaultGroup;
    @NotNull
    private Map<String, T> ftMap;
}
