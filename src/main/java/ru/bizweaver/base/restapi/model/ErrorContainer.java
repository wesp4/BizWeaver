package ru.bizweaver.base.restapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorContainer implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String code;
    private String type;
}
