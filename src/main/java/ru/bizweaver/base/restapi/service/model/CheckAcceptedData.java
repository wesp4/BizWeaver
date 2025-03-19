package ru.bizweaver.base.restapi.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.io.Serial;
import java.io.Serializable;

@Value
@Jacksonized
@Data
@Builder
@AllArgsConstructor
public class CheckAcceptedData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    String businessKey;
    String requestId;
}
