package ru.bizweaver.base.process.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


@Data
@AllArgsConstructor
public class AsyncProcess<T extends AbstractEventContext> implements Serializable {
    /**
     * Serial version UID.
     */
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * Идентификатор процесса.
     */
    private String id;

    /**
     * Имя процесса.
     */
    private String processName;

    /**
     * Контекст события.
     */
    private T eventContext;
}
