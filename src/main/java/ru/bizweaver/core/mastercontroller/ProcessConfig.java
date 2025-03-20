package ru.bizweaver.core.mastercontroller;

import io.micrometer.core.instrument.MeterRegistry;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.bizweaver.base.metric.MetricFactory;
import ru.bizweaver.base.metric.model.MetricsSettings;
import ru.bizweaver.base.process.ProcessFactory;
import ru.bizweaver.base.process.ProcessFactoryImpl;
import ru.bizweaver.base.process.delegate.ProcessManagerDelegate;
import ru.bizweaver.base.process.delegate.ProcessManagerDelegateImpl;
import ru.bizweaver.base.process.model.ProcessSettings;
import ru.bizweaver.base.process.service.ProcessService;
import ru.bizweaver.base.variable.CamundaVariable;
import ru.bizweaver.base.variable.CamundaVariableImp;
import ru.bizweaver.core.mastercontroller.model.MasterControllerEventContext;
import ru.bizweaver.core.mastercontroller.model.MasterControllerProcessContext;

@Configuration
public class ProcessConfig {

    @Bean
    @ConfigurationProperties("processes.master-controller")
    ProcessSettings<MasterControllerProcessContext> masterControllerProcessContextProcessSettings() {
        return new ProcessSettings<>();
    }

    @Bean
    CamundaVariable<MasterControllerProcessContext> masterControllerProcessContextVariable() {
        return new CamundaVariableImp<>("MASTER_CONTROLLER_PROCESS_CONTEXT");
    }

    @Bean
    CamundaVariable<MasterControllerEventContext> masterControllerEventContextVariable() {
        return new CamundaVariableImp<>("MASTER_CONTROLLER_EVENT_CONTEXT");
    }

    @Bean
    @ConfigurationProperties("metrics")
    MetricsSettings metricsSettings() {
        return new MetricsSettings();
    }

    @Bean
    MetricFactory metricFactory(
            MeterRegistry registry,
            MetricsSettings metricsSettings
    ) {
        return new MetricFactory(
                registry,
                metricsSettings
        );
    }

    @Bean
    ProcessFactory processFactory(
            RuntimeService runtimeService,
            MetricFactory metricFactory
    ) {
        return new ProcessFactoryImpl(runtimeService, metricFactory);
    }

    @Bean
    ProcessService<MasterControllerProcessContext> masterControllerProcessContextProcessService(
            ProcessFactory processFactory,
            ProcessSettings<MasterControllerProcessContext> masterControllerProcessContextProcessSettings,
            CamundaVariable<MasterControllerProcessContext> masterControllerProcessContextVariable,
            CamundaVariable<MasterControllerEventContext> masterControllerEventContextVariable
    ) {
        return processFactory.service(
                masterControllerProcessContextProcessSettings,
                masterControllerProcessContextVariable,
                masterControllerEventContextVariable,
                (businessKey, processContext) -> {
                },
                "masterController"
        );
    }

    @Bean
    ProcessManagerDelegate<MasterControllerProcessContext> masterControllerProcessManagerDelegate(
            ProcessService<MasterControllerProcessContext> masterControllerProcessContextProcessService
    ) {
        return new ProcessManagerDelegateImpl<>(
                "MASTER_CONTROLLER_CATCH_EVENT",
                masterControllerProcessContextProcessService
        );
    }
}
