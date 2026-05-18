package com.yw.local.task.message.config.aop;

import com.yw.local.task.message.LocalTaskMessage;
import com.yw.local.task.message.config.properties.LocalTaskMessageProperties;
import com.yw.local.task.message.domain.model.entity.LocalTaskMessageEntityCommand;
import com.yw.local.task.message.domain.service.ILocalTaskMessageHandleService;
import com.yw.local.task.message.infrastructure.support.HouseNumberRangeParser;
import com.yw.local.task.message.infrastructure.support.LocalTaskMessageHouseContext;
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
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * 本地消息切面。
 * <p>
 * 该切面负责两件事：
 * 1. 从被 {@code @LocalTaskMessage} 标记的方法中提取消息实体
 * 2. 把当前方法声明的门牌范围放入线程上下文，供仓储层落库时使用
 */
@Aspect
@Component
@Slf4j
public class LocalTaskMessageAop {

    private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private ILocalTaskMessageHandleService localTaskMessageHandleService;

    @Resource
    private LocalTaskMessageProperties localTaskMessageProperties;

    @Pointcut("@annotation(com.yw.local.task.message.LocalTaskMessage)")
    public void pointcut() {
    }

    @Around("pointcut() && @annotation(localTaskMessage)")
    public Object notify(ProceedingJoinPoint joinPoint, LocalTaskMessage localTaskMessage) throws Throwable {
        if (!localTaskMessageProperties.isEnabled()) {
            return joinPoint.proceed();
        }

        String signature = joinPoint.getSignature().toShortString();
        List<Integer> houseNumbers = HouseNumberRangeParser.parse(
                localTaskMessage.houses(),
                localTaskMessageProperties.getHouse().getTotalCount()
        );

        /*
         * 如果外部业务本身已经在事务里，就直接复用当前事务。
         * 如果没有事务，则由组件自己包一层，保证“业务方法执行 + 消息落库”处于同一个事务边界。
         */
        boolean transactionActive = TransactionSynchronizationManager.isActualTransactionActive();
        if (transactionActive) {
            return processLocalTaskMessage(joinPoint, signature, localTaskMessage.entityAttributeName(), localTaskMessage.houses(), houseNumbers);
        }

        try {
            return transactionTemplate.execute(status -> {
                try {
                    return processLocalTaskMessage(
                            joinPoint,
                            signature,
                            localTaskMessage.entityAttributeName(),
                            localTaskMessage.houses(),
                            houseNumbers
                    );
                } catch (Throwable e) {
                    status.setRollbackOnly();
                    throw new LocalTaskMessageAopRuntimeException(e);
                }
            });
        } catch (LocalTaskMessageAopRuntimeException e) {
            throw e.getCause();
        }
    }

    private Object processLocalTaskMessage(ProceedingJoinPoint joinPoint,
                                           String signature,
                                           String entityAttributeName,
                                           String housesExpression,
                                           List<Integer> houseNumbers) throws Throwable {
        /*
         * 这里需要保存旧上下文并在 finally 中恢复，
         * 否则嵌套调用被注解方法时，外层上下文会被内层覆盖掉。
         */
        LocalTaskMessageHouseContext.RouteContext previousContext = LocalTaskMessageHouseContext.get();
        LocalTaskMessageHouseContext.bind(new LocalTaskMessageHouseContext.RouteContext(signature, housesExpression, houseNumbers));
        try {
            Object result = joinPoint.proceed();
            LocalTaskMessageEntityCommand command = getLocalTaskMessageEntityCommand(joinPoint, entityAttributeName);
            if (command == null) {
                throw new IllegalArgumentException("未能从方法参数中提取 LocalTaskMessageEntityCommand，方法：" + signature);
            }

            LocalTaskMessageHouseContext.updateTaskName(command.getTaskName());
            localTaskMessageHandleService.handleLocalTaskMessage(command);
            return result;
        } catch (Throwable e) {
            log.error("处理本地任务消息失败，方法：{}", signature, e);
            throw e;
        } finally {
            LocalTaskMessageHouseContext.restore(previousContext);
        }
    }

    /**
     * 提取消息实体。
     * <p>
     * 先尝试从方法直接参数中查找；
     * 如果注解配置了属性路径，再按路径查找嵌套对象。
     */
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

    private LocalTaskMessageEntityCommand getNestedLocalTaskMessageEntityCommand(ProceedingJoinPoint joinPoint,
                                                                                 String entityAttributeName,
                                                                                 Object[] args) {
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
            log.error("读取消息实体嵌套属性失败，属性：{}，对象类型：{}", propertyName, source.getClass().getName(), e);
            return null;
        }
    }

    /**
     * 用运行时异常把 checked exception 带出 lambda。
     */
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
