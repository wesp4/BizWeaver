package ru.bizweaver.base.variable;

import ru.bizweaver.base.variable.exceptions.EmptyVariableBpmnException;

import org.camunda.bpm.engine.delegate.DelegateExecution;

import java.util.Optional;


public interface CamundaVariable<T> {
    /**
     * Метод получает хранимое значение переменной
     * и пытаеться привести его к объявленному типу переменной.
     *
     * @param execution - объект исеолнение процесса
     * @return - значение переменной типа Т
     */
    @SuppressWarnings("unchecked")
    T get(DelegateExecution execution);

    /**
     * Метод получает хранимое значение переменной
     * и пытаеться привести его к объявленному типу переменной.
     * Оборачивает его в {@link Optional}
     *
     * @param execution - объект исеолнение процесса
     * @return - значение переменной типа Т обернутое в {@link Optional}
     */
    @SuppressWarnings("unchecked")
    Optional<T> getOptional(DelegateExecution execution);

    /**
     * Метод получает хранимое значение переменной
     * и пытаеться привести его к объявленному типу переменной.
     * Если значение равно <strong>null</strong>,
     * то кадает исключение {@link EmptyVariableBpmnException}
     *
     * @param execution - объект исеолнение процесса
     * @return - значение переменной типа Т обернутое в {@link Optional}
     */
    @SuppressWarnings("unchecked")
    T getNonNull(DelegateExecution execution) throws EmptyVariableBpmnException;

    /**
     * Метод устанавливает в переменную объект типа Т
     *
     * @param execution - объект исеолнение процесса
     * @param value     - новое значение переменной
     */
    void set(DelegateExecution execution, T value);

    /**
     * Метод получает наименование переменной в Cavunda
     *
     * @return - значение переменной типа Т обернутое в {@link Optional}
     */
    String getVariableName();
}
