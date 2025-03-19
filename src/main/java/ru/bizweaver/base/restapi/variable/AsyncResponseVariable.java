package ru.bizweaver.base.restapi.variable;

import ru.bizweaver.base.restapi.model.AsyncResponse;
import ru.bizweaver.base.variable.CamundaVariableImp;

public class AsyncResponseVariable<T> extends CamundaVariableImp<AsyncResponse<T>> {

    public static String generateVariableName(String responseId) {
        return responseId;
    }

    public AsyncResponseVariable(String responseId) {
        super(generateVariableName(responseId));
    }
}
