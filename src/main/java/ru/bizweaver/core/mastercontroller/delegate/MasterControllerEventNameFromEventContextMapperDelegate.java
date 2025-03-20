package ru.bizweaver.core.mastercontroller.delegate;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import ru.bizweaver.base.variable.CamundaVariable;
import ru.bizweaver.core.mastercontroller.model.MasterControllerEventContext;

@Component
@RequiredArgsConstructor
public class MasterControllerEventNameFromEventContextMapperDelegate implements JavaDelegate {
    private final CamundaVariable<MasterControllerEventContext> masterControllerEventContextVariable;
    private final CamundaVariable<String> masterControllerEventNameDelegate;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        masterControllerEventNameDelegate.set(
                delegateExecution,
                masterControllerEventContextVariable.getNonNull(delegateExecution).getEvent()
        );
    }
}
