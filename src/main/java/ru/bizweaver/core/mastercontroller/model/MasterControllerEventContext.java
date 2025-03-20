package ru.bizweaver.core.mastercontroller.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.bizweaver.base.process.model.AbstractEventContext;
import ru.bizweaver.base.process.model.MdcContext;

import java.io.Serial;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class MasterControllerEventContext extends AbstractEventContext {
    @Serial
    private static final long serialVersionUID = 1L;

    private String testField;

    public MasterControllerEventContext(String businessKey, String callId, String event, MdcContext mdcContext) {
        super(businessKey, callId, event, mdcContext);
    }
}
