package ru.bizweaver.base.process.connector;

import ru.bizweaver.base.process.model.MdcContext;

import org.camunda.bpm.engine.delegate.DelegateExecution;

import java.util.Map;

public interface SendEventConnector {

    String getBusinessKey(DelegateExecution execution);

    String getCallId(DelegateExecution execution);

    MdcContext getMdcContext(DelegateExecution execution);

    Map<String, Object> getVariables(DelegateExecution execution);
}
