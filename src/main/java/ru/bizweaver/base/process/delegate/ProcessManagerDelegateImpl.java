package ru.bizweaver.base.process.delegate;

import org.springframework.util.CollectionUtils;
import ru.bizweaver.base.process.exception.EventCorrelationException;
import ru.bizweaver.base.process.model.AbstractEventContext;
import ru.bizweaver.base.process.model.AbstractProcessContext;
import ru.bizweaver.base.process.model.AsyncProcess;
import ru.bizweaver.base.process.service.CorrelationService;
import ru.bizweaver.base.process.service.CorrelationServiceImpl;
import ru.bizweaver.base.process.service.ProcessService;
import ru.bizweaver.base.process.variable.AsyncProcessesQueueVariable;
import ru.bizweaver.base.utils.ProcessUtils;
import ru.bizweaver.base.variable.CamundaVariable;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
public class ProcessManagerDelegateImpl<T extends AbstractProcessContext> implements
    ProcessManagerDelegate<T> {

    private final String correlationKey;
    private final RuntimeService runtimeService;
    private final CamundaVariable<T> processContextVariable;
    private final CamundaVariable<? extends AbstractEventContext> eventContextVariable;
    private final AsyncProcessesQueueVariable asyncProcessesQueueVariable;
    private final List<String> activeStories;
    private final CorrelationService correlationService;

    public ProcessManagerDelegateImpl(String correlationKey,
        ProcessService<T> processService) {
        this.correlationKey = correlationKey;
        this.runtimeService = processService.getRuntimeService();
        this.processContextVariable = processService.getProcessContextVariable();
        this.eventContextVariable = processService.getEventContextVariable();
        this.asyncProcessesQueueVariable = new AsyncProcessesQueueVariable(
            correlationKey.concat("_").concat(AsyncProcessesQueueVariable.DEFAULT_NAME));
        this.activeStories = processService.getActiveStories();
        this.correlationService = new CorrelationServiceImpl(runtimeService);
    }

    @Override
    public void scheduleAsyncProcess(DelegateExecution execution, String processName) {
        final AbstractEventContext eventContext = eventContextVariable.getNonNull(execution);
        final ConcurrentLinkedQueue<AsyncProcess<? extends AbstractEventContext>> queue = getQueue(
            execution);
        final boolean quickStart = CollectionUtils.isEmpty(queue);
        queue.add(new AsyncProcess<>(eventContext.getCallId(), processName, eventContext));
        asyncProcessesQueueVariable.set(execution, queue);
        if (quickStart) {
            startAsyncProcess(execution, queue);
        }
    }


    @Override
    public void finishAsyncProcessById(DelegateExecution execution) {
        final ConcurrentLinkedQueue<AsyncProcess<? extends AbstractEventContext>> queue = getQueue(
            execution);
        final AbstractEventContext eventContext = eventContextVariable.getNonNull(execution);
        final String logIds = ProcessUtils.getLogIds(eventContext);
        final String callId = eventContext.getCallId();
        log.debug("{} Finish process with event= {}", logIds, callId);
        Optional.ofNullable(queue.peek())
            .ifPresentOrElse(process -> {
                if (callId.equalsIgnoreCase(process.getId())) {
                    queue.poll();
                    log.debug("{} Process with event= {} is finished", logIds, callId);
                    asyncProcessesQueueVariable.set(execution, queue);
                    startAsyncProcess(execution, queue);
                }
            }, () -> log.debug("{} Finish process with event= {} is not found", logIds, callId));
    }

    @Override
    public void finishAsyncProcess(DelegateExecution execution) {
        final ConcurrentLinkedQueue<AsyncProcess<? extends AbstractEventContext>> queue = getQueue(
            execution);
        queue.poll();
        asyncProcessesQueueVariable.set(execution, queue);
        startAsyncProcess(execution, queue);
    }

    @Override
    public void sendFinishAsyncProcess(DelegateExecution execution, String finishEventName)
        throws EventCorrelationException {
        final AbstractEventContext eventContext = eventContextVariable.getNonNull(execution);
        final String logIds = ProcessUtils.getLogIds(eventContext);
        eventContext.setEvent(finishEventName);

        log.info("{} Sending internal event = {}, to process {}",
            logIds, eventContext.getCallId(),
            eventContext.getBusinessKey());

        final Map<String, Object> variables = new HashMap<>();
        variables.put(processContextVariable.getVariableName(),
            processContextVariable.get(execution));
        variables.put(eventContextVariable.getVariableName(), eventContext);
        correlationService.correlateEvent(correlationKey, eventContext, variables);
    }

    @Override
    public void sendFinishAsyncProcessWithRestResponse(DelegateExecution execution, String finishEventName)
        throws EventCorrelationException {
        final AbstractEventContext eventContext = eventContextVariable.getNonNull(execution);
        final String logIds = ProcessUtils.getLogIds(eventContext);
        eventContext.setEvent(finishEventName);

        log.info("{} Sending internal event = {}, to process {}",
            logIds, eventContext.getCallId(),
            eventContext.getBusinessKey());

        final Map<String, Object> variables = new HashMap<>();
        variables.put(processContextVariable.getVariableName(),
            processContextVariable.get(execution));
        variables.put(eventContextVariable.getVariableName(), eventContext);
        String callId = eventContext.getCallId();
        variables.put(callId, execution.getVariable(callId));
        correlationService.correlateEvent(correlationKey, eventContext, variables);
    }

    @Override
    public Boolean hasAnyAsyncProcess(DelegateExecution execution) {
        return !CollectionUtils.isEmpty(getQueue(execution));
    }

    @Override
    public Boolean eventEquals(DelegateExecution execution, String eventName) {
        return eventContextVariable.getOptional(execution)
            .map(eventContext -> eventName.equalsIgnoreCase(eventContext.getEvent()))
            .orElse(false);
    }

    @Override
    public Boolean useActiveStory(DelegateExecution execution, String story) {
        return activeStories.contains(story);
    }

    private void startAsyncProcess(DelegateExecution execution,
        ConcurrentLinkedQueue<AsyncProcess<? extends AbstractEventContext>> queue) {
        Optional.ofNullable(queue.peek())
            .ifPresent(process -> Optional.ofNullable(process.getEventContext())
                .ifPresent(context -> {
                    Map<String, Object> variables = new HashMap<>();
                    variables.put(processContextVariable.getVariableName(),
                        processContextVariable.get(execution));
                    variables.put(eventContextVariable.getVariableName(), context);
                    String callId = context.getCallId();
                    variables.put(callId, execution.getVariable(callId));
                    log.debug(
                        "{} Start process {} with businessKey: {}",
                        ProcessUtils.getLogIds(context),
                        process.getProcessName(),
                        process.getId());

                    CompletableFuture.runAsync(() -> {

                        runtimeService.createProcessInstanceByKey(process.getProcessName())
                            .setVariables(variables)
                            .businessKey(process.getId())
                            .execute();
                    });
                }));
    }

    private ConcurrentLinkedQueue<AsyncProcess<? extends AbstractEventContext>> getQueue(
        DelegateExecution execution) {
        return asyncProcessesQueueVariable.getOptional(execution)
            .orElse(new ConcurrentLinkedQueue<>());
    }
}
