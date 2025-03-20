package ru.bizweaver.core.mastercontroller.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.bizweaver.base.process.model.AbstractProcessContext;

import java.io.Serial;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class MasterControllerProcessContext extends AbstractProcessContext {
    @Serial
    private static final long serialVersionUID = 1L;

    private String testField;
}
