package ru.bizweaver.base.process.delegate;

import ru.bizweaver.base.process.exception.EventCorrelationException;
import ru.bizweaver.base.process.exception.StartProcessException;
import ru.bizweaver.base.process.model.AbstractEventContext;
import ru.bizweaver.base.process.service.ProcessService;
import ru.bizweaver.base.process.connector.EventStartConnector;
import ru.bizweaver.base.process.model.AbstractProcessContext;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class SendStartEventDelegate<P extends AbstractProcessContext> implements JavaDelegate {

    private final ProcessService<P> processService;
    private final EventStartConnector<? extends AbstractEventContext> eventStartConnector;
    private final String correlationKey;
    private final JavaDelegate errorCallBackDelegate;

    public SendStartEventDelegate(ProcessService<P> processService,
                                  EventStartConnector<? extends AbstractEventContext> eventStartConnector,
                                  String correlationKey) {
        this.processService = processService;
        this.eventStartConnector = eventStartConnector;
        this.correlationKey = correlationKey;
        this.errorCallBackDelegate = null;
    }

    public SendStartEventDelegate(ProcessService<P> processService,
                                  EventStartConnector<? extends AbstractEventContext> eventStartConnector,
                                  String correlationKey,
                                  JavaDelegate errorCallBackDelegate) {
        this.processService = processService;
        this.eventStartConnector = eventStartConnector;
        this.correlationKey = correlationKey;
        this.errorCallBackDelegate = errorCallBackDelegate;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        AbstractEventContext eventContext = eventStartConnector.getContext(execution);
        Map<String, Object> variables = eventStartConnector.getVariables(execution);
        CompletableFuture.runAsync(() ->
        {
            try {
                processService.sendExternalEvent(correlationKey, eventContext, variables);
            } catch (StartProcessException | EventCorrelationException e) {
                try {
                    if (Objects.nonNull(errorCallBackDelegate)) {
                        errorCallBackDelegate.execute(execution);
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }
}
