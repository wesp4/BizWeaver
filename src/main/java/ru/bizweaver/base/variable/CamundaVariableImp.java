package ru.bizweaver.base.variable;

import ru.bizweaver.base.variable.exceptions.EmptyVariableBpmnException;
import ru.bizweaver.base.variable.exceptions.TechnicalExceptionVariable;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;

import java.util.Optional;

@Getter
@RequiredArgsConstructor
public class CamundaVariableImp<T> implements CamundaVariable<T> {

    private final String variableName;

    @Override
    @SuppressWarnings("unchecked")
    public T get(DelegateExecution execution) {
        return (T) execution.getVariable(variableName);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<T> getOptional(DelegateExecution execution) {
        return Optional.ofNullable((T) execution.getVariable(variableName));
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull T getNonNull(DelegateExecution execution) throws EmptyVariableBpmnException {
        try {
            return Optional.ofNullable((T) execution.getVariable(variableName))
                .orElseThrow(() -> new EmptyVariableBpmnException(variableName));
        } catch (EmptyVariableBpmnException emptyVariableBpmnException) {
            throw emptyVariableBpmnException;
        } catch (Exception exception) {
            throw new TechnicalExceptionVariable(variableName, exception.getMessage());
        }
    }

    @Override
    public void set(DelegateExecution execution, T value) {
        execution.setVariable(variableName, value);
    }
}
