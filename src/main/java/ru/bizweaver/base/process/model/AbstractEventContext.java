package ru.bizweaver.base.process.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;


@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractEventContext implements Serializable {
    @Serial
    private static final long serialVersionUID = 1;
    /**
     * Бизнес-ключ процесса
     */
    protected String businessKey;
    /**
     * идентификатор события
     */
    protected String callId;

    /**
     * Ключ события
     */
    protected String event;

    /**
     * Ключ действия
     */
    @Deprecated
    protected String action;

    /**
     * Контекст {@link org.slf4j.MDC}.
     */
    protected MdcContext mdcContext;

    /**
     * Бизнес-ключ вызываемого процесса
     */
    @Deprecated
    protected String callbackBusinessKey;
}
