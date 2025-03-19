package ru.bizweaver.base.error;


import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

@Setter
@RequiredArgsConstructor
public class ErrorDelegate {

    private final ErrorsVariable errorVariable;

    public void registry(DelegateExecution execution, String codeValue, String typeValue) {
        errorVariable.setError(execution, typeValue, codeValue);
    }

    public boolean hasError(DelegateExecution execution) {
        return errorVariable.getOptional(execution)
                .map(CollectionUtils::isEmpty)
                .map(empty -> !empty)
                .orElse(Boolean.FALSE);
    }

    public boolean hasErrorType(DelegateExecution execution, String typeError) {
        return errorVariable.getOptional(execution)
                .stream()
                .flatMap(List::stream)
                .anyMatch(errorContainer -> Optional.ofNullable(errorContainer.getType())
                        .map(errorType -> errorType.equals(typeError))
                        .orElse(Boolean.FALSE));
    }
}
