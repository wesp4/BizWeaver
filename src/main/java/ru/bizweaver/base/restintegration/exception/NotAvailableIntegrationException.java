/*
 * VTB Group. Do not reproduce without permission in writing.
 * Copyright (c) 2020 VTB Group. All rights reserved.
 */
package ru.bizweaver.base.restintegration.exception;

import org.camunda.bpm.engine.delegate.BpmnError;

/**
 * Исключение, связанное с недоступным МКС для интеграции.
 *
 * @author NTB Team
 */
public class NotAvailableIntegrationException extends BpmnError {

    public static final String SUFFIX = "NOT_AVAILABLE";
    private static final String CODE_PATTERN = "%s_%s";

    public NotAvailableIntegrationException(String code) {
        super(CODE_PATTERN.formatted(code, SUFFIX));
    }
}
