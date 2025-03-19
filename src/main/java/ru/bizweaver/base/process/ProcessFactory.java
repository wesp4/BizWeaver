package ru.bizweaver.base.process;

import ru.bizweaver.base.process.model.AbstractEventContext;
import ru.bizweaver.base.process.model.AbstractProcessContext;
import ru.bizweaver.base.process.model.ProcessSettings;
import ru.bizweaver.base.process.service.ProcessService;
import ru.bizweaver.base.variable.CamundaVariable;

import java.util.List;
import java.util.function.BiConsumer;

public interface ProcessFactory {
    <P extends AbstractProcessContext> ProcessService<P> service(ProcessSettings<P> processSettings,
                                                                 CamundaVariable<P> processContextVariable,
                                                                 CamundaVariable<? extends AbstractEventContext> eventContextVarVariable,
                                                                 BiConsumer<String, P> createFunction,
                                                                 String metricGroup);

    List<String> getActiveStories();
}
