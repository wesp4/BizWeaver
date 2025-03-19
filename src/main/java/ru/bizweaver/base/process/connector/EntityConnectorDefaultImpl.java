package ru.bizweaver.base.process.connector;

import ru.bizweaver.base.process.model.AbstractEventContext;
import ru.bizweaver.base.variable.CamundaVariable;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;

@RequiredArgsConstructor
public class EntityConnectorDefaultImpl<T> implements EntityConnector<T> {

    private final CamundaVariable<? extends AbstractEventContext> eventContextVariable;
    private final CamundaVariable<T> entityVariable;

    @Override
    public String getBusinessKey(DelegateExecution execution) {
        return eventContextVariable.getNonNull(execution).getBusinessKey();
    }

    @Override
    public void setEntity(DelegateExecution execution, T entity) {
        entityVariable.set(execution, entity);
    }

    @Override
    public T getEntity(DelegateExecution execution) {
        return entityVariable.get(execution);
    }
}
