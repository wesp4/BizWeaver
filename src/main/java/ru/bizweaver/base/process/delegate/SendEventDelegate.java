package ru.bizweaver.base.process.delegate;

import ru.bizweaver.base.process.connector.SendEventConnector;
import ru.bizweaver.base.process.model.AbstractProcessContext;
import ru.bizweaver.base.process.model.MdcContext;
import ru.bizweaver.base.process.service.ProcessService;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class SendEventDelegate<P extends AbstractProcessContext> implements JavaDelegate {

    private final ProcessService<P> targetProcessService;
    private final String correlationKey;
    private final SendEventConnector sendEventConnector;

    public SendEventDelegate(ProcessService<P> targetProcessService,
                             SendEventConnector sendEventConnector,
                             String correlationKey) {
        this.targetProcessService = targetProcessService;
        this.correlationKey = correlationKey;
        this.sendEventConnector = sendEventConnector;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String businessKey = sendEventConnector.getBusinessKey(execution);
        String callId = sendEventConnector.getCallId(execution);
        MdcContext mdcContext = sendEventConnector.getMdcContext(execution);
        Map<String, Object> variables = sendEventConnector.getVariables(execution);

        CompletableFuture.runAsync(() -> {
            try {
                targetProcessService.sendInternalEvent(
                    correlationKey,
                    businessKey,
                    callId,
                    mdcContext,
                    variables
                );
            } catch (Throwable e) {
                throw new RuntimeException();
            }
        });
    }
}
