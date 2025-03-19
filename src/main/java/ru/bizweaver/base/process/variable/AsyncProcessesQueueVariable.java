package ru.bizweaver.base.process.variable;

import ru.bizweaver.base.process.model.AbstractEventContext;
import ru.bizweaver.base.process.model.AsyncProcess;
import ru.bizweaver.base.variable.CamundaVariableImp;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Класс переменных Camunda для хранения очереди последовательных процессов.
 *
 * @author NTB Team
 */
public class AsyncProcessesQueueVariable
        extends
    CamundaVariableImp<ConcurrentLinkedQueue<AsyncProcess<? extends AbstractEventContext>>> {
    public static final String DEFAULT_NAME = "ASYNC_PROCESSES_QUEUE";

    public AsyncProcessesQueueVariable(String variableName) {
        super(variableName);
    }

    public AsyncProcessesQueueVariable() {
        super(DEFAULT_NAME);
    }

    public ConcurrentLinkedQueue<AsyncProcess<? extends AbstractEventContext>> createQueue() {
        return new ConcurrentLinkedQueue<>();
    }
}
