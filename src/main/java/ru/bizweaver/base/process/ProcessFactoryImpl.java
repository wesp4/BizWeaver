package ru.bizweaver.base.process;

import ru.bizweaver.base.metric.MetricFactory;
import ru.bizweaver.base.metric.delegate.MetricDelegate;
import ru.bizweaver.base.process.model.AbstractEventContext;
import ru.bizweaver.base.process.model.AbstractProcessContext;
import ru.bizweaver.base.process.model.ProcessSettings;
import ru.bizweaver.base.process.service.ProcessService;
import ru.bizweaver.base.process.service.ProcessServiceImpl;
import ru.bizweaver.base.variable.CamundaVariable;

import org.camunda.bpm.engine.RuntimeService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class ProcessFactoryImpl implements ProcessFactory {

    private final RuntimeService runtimeService;
    private final MetricFactory metricFactory;
    private final List<String> activeStories;

    public ProcessFactoryImpl(RuntimeService runtimeService,
                              MetricFactory metricFactory,
                              List<String> activeStories) {
        this.runtimeService = runtimeService;
        this.metricFactory = metricFactory;
        this.activeStories = activeStories;
    }

    public ProcessFactoryImpl(RuntimeService runtimeService,
                              MetricFactory metricFactory) {
        this.runtimeService = runtimeService;
        this.metricFactory = metricFactory;
        this.activeStories = new ArrayList<>();
    }

    @Override
    public <P extends AbstractProcessContext> ProcessService<P> service(ProcessSettings<P> processSettings,
                                                                        CamundaVariable<P> processContextVariable,
                                                                        CamundaVariable<? extends AbstractEventContext> eventContextVarVariable,
                                                                        BiConsumer<String, P> createFunction,
                                                                        String metricGroup) {
        MetricDelegate metricDelegate = metricFactory.delegate(metricGroup);
        return new ProcessServiceImpl<>(
            processContextVariable,
            eventContextVarVariable,
            processSettings,
            activeStories,
            runtimeService,
            createFunction,
            metricDelegate
            );
    }

    @Override
    public List<String> getActiveStories() {
        return activeStories;
    }
}
