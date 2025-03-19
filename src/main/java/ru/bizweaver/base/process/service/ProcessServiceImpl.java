package ru.bizweaver.base.process.service;

import ru.bizweaver.base.metric.delegate.MetricDelegate;
import ru.bizweaver.base.process.exception.EmptyProcessVariableException;
import ru.bizweaver.base.process.exception.EventCorrelationException;
import ru.bizweaver.base.process.exception.StartProcessException;
import ru.bizweaver.base.process.model.AbstractEventContext;
import ru.bizweaver.base.process.model.AbstractProcessContext;
import ru.bizweaver.base.process.model.MdcContext;
import ru.bizweaver.base.process.model.ProcessSettings;
import ru.bizweaver.base.process.model.ProcessSettings.EntityIntegrationProperties;
import ru.bizweaver.base.utils.ProcessUtils;
import ru.bizweaver.base.variable.CamundaVariable;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

@Slf4j
public class ProcessServiceImpl<P extends AbstractProcessContext> implements ProcessService<P> {

    private final CamundaVariable<P> processContextVariable;
    private final CamundaVariable<? extends AbstractEventContext> eventContextVariable;
    private final ProcessSettings<P> processSettings;
    private final List<String> activeStories;
    private final RuntimeService runtimeService;
    private final CorrelationService correlationService;
    private final BiConsumer<String, P> createFunction;
    private final MetricDelegate metricDelegate;

    public ProcessServiceImpl(CamundaVariable<P> processContextVariable,
                              CamundaVariable<? extends AbstractEventContext> eventContextVariable,
                              ProcessSettings<P> processSettings,
                              List<String> activeStories,
                              RuntimeService runtimeService,
                              BiConsumer<String, P> createFunction,
                              MetricDelegate metricDelegate) {
        this.processContextVariable = processContextVariable;
        this.eventContextVariable = eventContextVariable;
        this.processSettings = processSettings;
        this.activeStories = activeStories;
        this.runtimeService = runtimeService;
        this.correlationService = new CorrelationServiceImpl(runtimeService);
        this.createFunction = createFunction;
        this.metricDelegate = metricDelegate;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getVariableByName(String name, String businessKey) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
            .processDefinitionKey(processSettings.getMasterProcessKey())
            .processInstanceBusinessKey(businessKey)
            .rootProcessInstances()
            .singleResult();
        return (T) Optional.ofNullable(processInstance)
            .map(ProcessInstance::getId)
            .map(id -> runtimeService.getVariable(id, name))
            .orElseThrow(() -> new EmptyProcessVariableException(name, businessKey));
    }

    @Override
    public <S> void setVariableByName(String name, String businessKey, S value) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
            .processDefinitionKey(processSettings.getMasterProcessKey())
            .processInstanceBusinessKey(businessKey)
            .rootProcessInstances()
            .singleResult();

        Optional.ofNullable(processInstance)
            .map(Execution::getId)
            .ifPresent(executionId -> runtimeService.setVariable(
                executionId, name, value));
    }

    @Override
    public CamundaVariable<? extends AbstractEventContext> getEventContextVariable() {
        return eventContextVariable;
    }

    @Override
    public CamundaVariable<P> getProcessContextVariable() {
        return processContextVariable;
    }

    @Override
    public RuntimeService getRuntimeService() {
        return runtimeService;
    }

    @Override
    public List<String> getActiveStories() {
        return activeStories;
    }

    @Override
    public void sendExternalEvent(String correlationKey, AbstractEventContext context, Map<String, Object> variables)
        throws EventCorrelationException, StartProcessException {
        final String logIds = ProcessUtils.getLogIds(context);
        final String businessKey = context.getBusinessKey();
        ProcessUtils.updateContext(context.getMdcContext());
        ProcessInstance processInstance = getProcessInstance(businessKey);

        if (processInstance == null) {
            try {
                log.debug("{} Creating process {}", logIds, processSettings.getMasterProcessKey());
                createProcessInstance(businessKey);
            } catch (ProcessEngineException e) {
                log.warn("{} Could not create process {}", logIds, processSettings.getMasterProcessKey());
                processInstance = getProcessInstance(businessKey);
                if (processInstance == null) {
                    log.error("{} Не удалось обработать событие {} для процесса {}. Не удалось создать процесс",
                        logIds, context.getEvent(), processSettings.getMasterProcessKey());
                    throw new StartProcessException();
                }
            }
        }

        try {
            Map<String, Object> correlationVariables = new HashMap<>(variables);
            correlationVariables.put(eventContextVariable.getVariableName(), context);

            correlationService.correlateEventRetryable(correlationKey, context, correlationVariables);
        } catch (Exception ex) {
            log.error("{} Не удалось обработать событие {} для процесса {}. Не удалось выполнить корреляцию",
                logIds, context.getEvent(), processSettings.getMasterProcessKey());
            throw ex;
        }
    }

    @Override
    public void sendInternalEvent(String correlationKey,
                                  String businessKey,
                                  String callId,
                                  MdcContext mdcContext,
                                  Map<String, Object> variables)
        throws EventCorrelationException {
        final String logIds = ProcessUtils.getLogIds(businessKey, callId);
        ProcessUtils.updateContext(mdcContext);
        try {
            correlationService.correlateRetryable(correlationKey, callId, callId, variables);
        } catch (Exception ex) {
            log.error("{} Не удалось обработать событие для процесса {}. Не удалось выполнить корреляцию",
                logIds, processSettings.getMasterProcessKey());
            throw ex;
        }
    }

    @Override
    public MetricDelegate getMetricDelegate() {
        return metricDelegate;
    }

    @Override
    public EntityIntegrationProperties getEntityIntegrationSettings() {
        return processSettings.getEntity();
    }


    @SuppressWarnings("unchecked")
    private void createProcessInstance(String businessKey) {
        Map<String, Object> processVariables = new HashMap<>();
        P context = (P) processSettings.getContext().clone();
        createFunction.accept(businessKey, context);
        processVariables.put(processContextVariable.getVariableName(), context);
        runtimeService.createProcessInstanceByKey(processSettings.getMasterProcessKey())
            .setVariables(processVariables)
            .businessKey(businessKey)
            .execute();
    }

    private ProcessInstance getProcessInstance(String businessKey) {
        return runtimeService.createProcessInstanceQuery()
            .processDefinitionKey(processSettings.getMasterProcessKey())
            .processInstanceBusinessKey(businessKey)
            .rootProcessInstances()
            .singleResult();
    }
}
