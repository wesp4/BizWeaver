package ru.bizweaver.base.process.model;


import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.slf4j.MDC;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
public class MdcContext implements Serializable {
    /**
     * Serial version UID.
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private final String unc;
    private String mdmId;
    private final String callId;
    private final String messageId;
    private final String methodName;
    private final String sessionId;
    private final String initiatorHost;
    private final String initiatorService;
    private final String channel;

    public static MdcContext buildContext() {
        final Map<String, String> contextCopy = MDC.getCopyOfContextMap();
        return MdcContext.builder()
                .callId(contextCopy.get("callId"))
                .messageId(contextCopy.get("messageId"))
                .methodName(contextCopy.get("methodName"))
                .sessionId(contextCopy.get("sessionId"))
                .initiatorHost(contextCopy.get("initiatorHost"))
                .initiatorService(contextCopy.get("initiatorService"))
                .build();
    }
}
