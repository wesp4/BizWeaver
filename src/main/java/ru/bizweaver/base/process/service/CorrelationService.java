package ru.bizweaver.base.process.service;

import ru.bizweaver.base.process.exception.EventCorrelationException;
import ru.bizweaver.base.process.model.AbstractEventContext;

import java.util.Map;

public interface CorrelationService {

    void correlateEventRetryable(String correlationKey,
                                 AbstractEventContext eventContext,
                                 Map<String, Object> variables
    ) throws EventCorrelationException;

    void correlateRetryable(String correlationKey,
                            String businessKey,
                            String callId,
                            Map<String, Object> variables
    ) throws EventCorrelationException;

    void correlateEvent(String correlationKey,
                        AbstractEventContext eventContext,
                        Map<String, Object> variables
    ) throws EventCorrelationException;

    void correlate(String correlationKey,
                   String businessKey,
                   String callId,
                   Map<String, Object> variables
    ) throws EventCorrelationException;
}
