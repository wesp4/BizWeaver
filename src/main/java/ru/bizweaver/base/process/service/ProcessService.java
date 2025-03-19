package ru.bizweaver.base.process.service;

import ru.bizweaver.base.metric.delegate.MetricDelegate;
import ru.bizweaver.base.process.exception.EventCorrelationException;
import ru.bizweaver.base.process.exception.StartProcessException;
import ru.bizweaver.base.process.model.AbstractEventContext;
import ru.bizweaver.base.process.model.AbstractProcessContext;
import ru.bizweaver.base.process.model.MdcContext;
import ru.bizweaver.base.process.model.ProcessSettings.EntityIntegrationProperties;
import ru.bizweaver.base.variable.CamundaVariable;

import org.camunda.bpm.engine.RuntimeService;

import java.util.List;
import java.util.Map;

public interface ProcessService<P extends AbstractProcessContext> {
    <T> T getVariableByName(String name, String businessKey);

    <S> void setVariableByName(String name, String businessKey, S value);

    CamundaVariable<? extends AbstractEventContext> getEventContextVariable();

    CamundaVariable<P> getProcessContextVariable();

    RuntimeService getRuntimeService();

    List<String> getActiveStories();

    void sendExternalEvent(String correlationKey, AbstractEventContext context, Map<String, Object> variables) throws EventCorrelationException, StartProcessException;

    void sendInternalEvent(String correlationKey,
                           String businessKey,
                           String callId,
                           MdcContext mdcContext,
                           Map<String, Object> variables) throws EventCorrelationException;

    MetricDelegate getMetricDelegate();

    EntityIntegrationProperties getEntityIntegrationSettings();
}