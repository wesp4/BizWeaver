package ru.bizweaver.base.process.service;

import ru.bizweaver.base.process.exception.EventCorrelationException;
import ru.bizweaver.base.process.model.AbstractEventContext;
import ru.bizweaver.base.utils.ProcessUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class CorrelationServiceImpl implements CorrelationService {

    private static final int RETRY_MAX_ATTEMPTS = 4;
    private static final int RETRY_DELAY = 1000;
    private static final int RETRY_DELAY_MULTIPLIER = 2;

    /**
     * Сервис Camunda {@link RuntimeService}
     */
    private final RuntimeService runtimeService;

    @Override
    public void correlateEventRetryable(String correlationKey,
                                        AbstractEventContext eventContext,
                                        Map<String, Object> variables) throws EventCorrelationException {
        correlateEventRetryableRecursive(correlationKey, eventContext, variables, RETRY_MAX_ATTEMPTS);
    }

    @Override
    @Retryable(
        value = EventCorrelationException.class,
        maxAttempts = RETRY_MAX_ATTEMPTS,
        backoff = @Backoff(delay = RETRY_DELAY, multiplier = RETRY_DELAY_MULTIPLIER))
    public void correlateRetryable(String correlationKey,
                                   String businessKey,
                                   String callId,
                                   Map<String, Object> variables
    ) throws EventCorrelationException {
        correlate(correlationKey, businessKey, callId, variables);
    }

    @Override
    public void correlateEvent(String correlationKey,
                               AbstractEventContext eventContext,
                               Map<String, Object> variables
    ) throws EventCorrelationException {
        final String logIds = ProcessUtils.getLogIds(eventContext.getBusinessKey(), eventContext.getCallId());

        try {
            log.debug("{} Start correlation event= {} by correlationKey= {} with businessKey= {}",
                logIds, eventContext.getEvent(), correlationKey, eventContext.getBusinessKey());
            runtimeService.correlateMessage(correlationKey, eventContext.getBusinessKey(), variables);
        } catch (Exception ex) {
            log.warn("{} Could not correlate event= {} by correlationKey= {} with businessKey= {}",
                logIds, eventContext.getEvent(), correlationKey, eventContext.getBusinessKey());
            throw new EventCorrelationException();
        }
    }

    @Override
    public void correlate(String correlationKey,
                          String businessKey,
                          String callId,
                          Map<String, Object> variables
    ) throws EventCorrelationException {
        final String logIds = ProcessUtils.getLogIds(businessKey, callId);

        try {
            log.debug("{} Start correlation by correlationKey= {} with businessKey= {}",
                logIds, correlationKey, businessKey);
            runtimeService.correlateMessage(correlationKey, businessKey, variables);
        } catch (Exception ex) {
            log.warn("{} Could not correlate by correlationKey= {} with businessKey= {}",
                logIds, correlationKey, businessKey);
            throw new EventCorrelationException();
        }
    }

    private void correlateEventRetryableRecursive(String correlationKey,
                                                  AbstractEventContext eventContext,
                                                  Map<String, Object> variables,
                                                  int maxAttempts
    ) throws EventCorrelationException {
        try {
            correlateEvent(correlationKey, eventContext, variables);
        } catch (EventCorrelationException ex) {
            if (maxAttempts > 0) {
                try {
                    Thread.sleep(RETRY_DELAY);
                    correlateEventRetryableRecursive(
                        correlationKey,
                        eventContext,
                        variables,
                        maxAttempts - 1
                    );
                } catch (InterruptedException e) {
                    throw ex;
                }
            } else {
                throw ex;
            }
        }
    }
}
