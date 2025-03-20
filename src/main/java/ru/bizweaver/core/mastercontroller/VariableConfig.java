
package ru.bizweaver.core.mastercontroller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.bizweaver.base.variable.CamundaVariable;
import ru.bizweaver.base.variable.CamundaVariableImp;

@Configuration
public class VariableConfig {
    @Bean
    CamundaVariable<String> masterControllerEventNameDelegate() {
        return new CamundaVariableImp<>("EVENT_NAME");
    }
}
