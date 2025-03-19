package ru.bizweaver.base.error;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import ru.bizweaver.base.restapi.model.ErrorContainer;
import ru.bizweaver.base.variable.CamundaVariableImp;

import java.util.ArrayList;
import java.util.List;

public class ErrorsVariable extends CamundaVariableImp<List<ErrorContainer>> {
    public static final String DEFAULT_NAME = "ERRORS";

    public ErrorsVariable(String variableName) {
        super(variableName);
    }

    public List<ErrorContainer> getErrors(DelegateExecution execution) {
        return this.getOptional(execution).orElseGet(ArrayList::new);
    }

    public void setError(DelegateExecution execution,
                         String errorCodeValue, String errorTypeValue) {
        final List<ErrorContainer> errors = this.getOptional(execution).orElseGet(ArrayList::new);
        errors.add(new ErrorContainer(errorCodeValue, errorTypeValue));
        this.set(execution, errors);
    }

}
