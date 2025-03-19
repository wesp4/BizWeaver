package ru.bizweaver.base.utils;

import static java.util.Optional.ofNullable;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.MDC;
import ru.bizweaver.base.process.model.AbstractEventContext;
import ru.bizweaver.base.process.model.MdcContext;
import ru.bizweaver.base.variable.CamundaVariable;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

@Slf4j
public class ProcessUtils {
    public static String getLogIds(String businessKey, String callId) {
        return "[%s][%s]".formatted(
                ofNullable(businessKey).orElse("unknown"),
                ofNullable(callId).orElse("unknown"));
    }

    public static <T extends AbstractEventContext> String getLogIds(T eventContext) {
        return ofNullable(eventContext)
            .map(context -> ProcessUtils.getLogIds(
                context.getBusinessKey(), context.getCallId()))
            .orElse(ProcessUtils.getLogIds("unknown", "unknown"));
    }

    public static <T extends AbstractEventContext> void addMdcContextDelegate(
        JoinPoint joinPoint, DelegateExecution execution,
        CamundaVariable<T> eventContextVariable
    ) {
        eventContextVariable.getOptional(execution)
            .map(AbstractEventContext::getMdcContext)
            .ifPresent(ProcessUtils::updateContext);
    }

    public static <T extends AbstractEventContext> void logReturningDelegate(
        JoinPoint joinPoint, DelegateExecution execution, Object result,
        CamundaVariable<T> eventContextVariable, String prefix
    ) {
        final AbstractEventContext eventContext = eventContextVariable.get(execution);
        final Object[] args = Arrays.stream(joinPoint.getArgs())
            .filter(arg -> !(arg instanceof DelegateExecution)).toArray();
        final String argsStr = ArrayUtils.isNotEmpty(args) ? " " + Arrays.toString(args) : "";

        if (result != null) {
            log.info(
                "{} {} {} {}{} return: {}",
                ProcessUtils.getLogIds(eventContext),
                prefix,
                execution.getCurrentActivityId(),
                joinPoint.getSignature().toShortString(), argsStr, result);
        } else {
            log.info(
                "{} {} {} {}{} finished",
                ProcessUtils.getLogIds(eventContext),
                prefix,
                execution.getCurrentActivityId(),
                joinPoint.getSignature().toShortString(), argsStr);
        }
    }

    public static <T extends AbstractEventContext> void logThrowingDelegate(
        JoinPoint joinPoint, DelegateExecution execution, BpmnError ex,
        CamundaVariable<T> eventContextVariable, String prefix
    ) {
        final AbstractEventContext eventContext = eventContextVariable.get(execution);
        final Object[] args = Arrays.stream(joinPoint.getArgs())
            .filter(arg -> !(arg instanceof DelegateExecution)).toArray();
        final String argsStr = ArrayUtils.isNotEmpty(args) ? " " + Arrays.toString(args) : "";

        log.error(
            "{} {} Catch exception {}{} in processing {} {}{}",
            ProcessUtils.getLogIds(eventContext),
            prefix,
            ex.getClass().getSimpleName(),
            StringUtils.isNotEmpty(ex.getErrorCode()) ? " with code: %s".formatted(ex.getErrorCode()) : "",
            execution.getCurrentActivityId(),
            joinPoint.getSignature().toShortString(), argsStr);
    }

    public static <T extends AbstractEventContext> void logSetVariable(
        JoinPoint joinPoint, DelegateExecution execution, Object value,
        CamundaVariable<T> eventContextVariable) {
        final AbstractEventContext eventContext = eventContextVariable.get(execution);
        log.trace("{} Set variable: {}", ProcessUtils.getLogIds(eventContext), value);
    }

    public static void updateContext(MdcContext mdcContext) {
        if (Objects.nonNull(mdcContext)) {
            MDC.put("callId", mdcContext.getCallId());
            MDC.put("methodName", mdcContext.getMethodName());
            MDC.put("sessionId", mdcContext.getSessionId());
            MDC.put("initiatorHost", mdcContext.getInitiatorHost());
            MDC.put("initiatorService", mdcContext.getInitiatorService());

            final String messageId = mdcContext.getMessageId();
            if (Objects.nonNull(messageId)) {
                MDC.put("messageId", messageId);
            } else {
                String randomUUID = UUID.randomUUID().toString();
                MDC.put("messageId", randomUUID);
            }
        }
    }
}