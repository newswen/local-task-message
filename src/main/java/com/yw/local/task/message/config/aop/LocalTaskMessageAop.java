package com.yw.local.task.message.config.aop;

import com.yw.local.task.message.LocalTaskMessage;
import com.yw.local.task.message.domain.model.entity.LocalTaskMessageEntityCommand;
import com.yw.local.task.message.domain.service.ILocalTaskMessageHandleService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.stereotype.Component;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @Author: yw
 * @Date: 2026/5/14 09:17
 * @Description:
 **/
//切面-把通用功能模块化的类
@Aspect
@Component
@Slf4j
public class LocalTaskMessageAop {

    private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private ILocalTaskMessageHandleService localTaskMessageHandleService;

    /**
     * 切点-从哪里切入，注解定义的地方
     */
    @Pointcut("@annotation(com.yw.local.task.message.LocalTaskMessage)")
    public void pointcut() {
    }

    @Around("pointcut() && @annotation(localTaskMessage)")
    public Object notify(ProceedingJoinPoint joinPoint, LocalTaskMessage localTaskMessage) throws Throwable {
        //获取切入点完整方法签名
        String signature = joinPoint.getSignature().toShortString();
        String entityAttributeName = localTaskMessage.entityAttributeName();

        // 判断【当前是否存在真实的、活跃的事务】
        boolean isActive = TransactionSynchronizationManager.isActualTransactionActive();
        //1.有事务就直接处理消息
        if (isActive) {
            return processLocalTaskMessage(joinPoint, signature, entityAttributeName);
        }

        //2.无事务就创建事务
        try {
            return transactionTemplate.execute(status -> {
                try {
                    return processLocalTaskMessage(joinPoint, signature, entityAttributeName);
                } catch (Throwable e) {
                    status.setRollbackOnly();
                    throw new LocalTaskMessageAopRuntimeException(e);
                }
            });
        } catch (LocalTaskMessageAopRuntimeException e) {
            throw e.getCause();
        }

    }

    private Object processLocalTaskMessage(ProceedingJoinPoint joinPoint, String signature, String entityAttributeName) throws Throwable {
        try {
            //1. 执行具体方法
            Object result = joinPoint.proceed();
            //从切入点方法中的参数结合注解内参数去获取本地消息实体
            LocalTaskMessageEntityCommand localTaskMessageEntityCommand = getLocalTaskMessageEntityCommand(joinPoint, entityAttributeName);
            if (localTaskMessageEntityCommand != null) {
                //2. 执行本地消息相关
                localTaskMessageHandleService.handleLocalTaskMessage(localTaskMessageEntityCommand);
            } else {
                log.error("获取任务消息实体失败 执行方法入口：{} 消息实体：{}", signature, entityAttributeName);
            }
            return result;
        } catch (Throwable e) {
            log.error("处理任务消息失败 - 错误: {}", e.getMessage(), e);
            throw e;
        }
    }

    private LocalTaskMessageEntityCommand getLocalTaskMessageEntityCommand(ProceedingJoinPoint joinPoint, String entityAttributeName) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return null;
        }
        if (!StringUtils.hasText(entityAttributeName)) {
            return getDirectLocalTaskMessageEntityCommand(args);
        }
        return getNestedLocalTaskMessageEntityCommand(joinPoint, entityAttributeName, args);
    }

    private LocalTaskMessageEntityCommand getDirectLocalTaskMessageEntityCommand(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof LocalTaskMessageEntityCommand) {
                return (LocalTaskMessageEntityCommand) arg;
            }
        }
        return null;
    }

    private LocalTaskMessageEntityCommand getNestedLocalTaskMessageEntityCommand(ProceedingJoinPoint joinPoint, String entityAttributeName, Object[] args) {
        String[] attributePath = entityAttributeName.split("\\.");
        if (attributePath.length == 0) {
            return null;
        }

        String[] parameterNames = getParameterNames(joinPoint);
        if (parameterNames.length == 0) {
            return null;
        }

        for (int i = 0; i < parameterNames.length && i < args.length; i++) {
            if (!attributePath[0].equals(parameterNames[i])) {
                continue;
            }
            Object value = args[i];
            for (int j = 1; j < attributePath.length; j++) {
                value = getPropertyValue(value, attributePath[j]);
                if (value == null) {
                    return null;
                }
            }
            if (value instanceof LocalTaskMessageEntityCommand) {
                return (LocalTaskMessageEntityCommand) value;
            }
            return null;
        }
        return null;
    }

    private String[] getParameterNames(ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = AopUtils.getMostSpecificMethod(methodSignature.getMethod(), joinPoint.getTarget().getClass());
        String[] parameterNames = PARAMETER_NAME_DISCOVERER.getParameterNames(method);
        return parameterNames == null ? new String[0] : parameterNames;
    }

    private Object getPropertyValue(Object source, String propertyName) {
        if (source == null) {
            return null;
        }
        if (source instanceof Map) {
            return ((Map<?, ?>) source).get(propertyName);
        }
        BeanWrapper beanWrapper = new BeanWrapperImpl(source);
        try {
            return beanWrapper.getPropertyValue(propertyName);
        } catch (BeansException e) {
            log.error("读取任务消息实体属性失败 属性：{} 对象类型：{}", propertyName, source.getClass().getName(), e);
            return null;
        }
    }

    private static class LocalTaskMessageAopRuntimeException extends RuntimeException {

        private final Throwable cause;

        private LocalTaskMessageAopRuntimeException(Throwable cause) {
            super(cause);
            this.cause = cause;
        }

        @Override
        public Throwable getCause() {
            return cause;
        }
    }

}
