package com.yw.local.task.message.config.condition;

import com.yw.local.task.message.config.properties.LocalTaskMessageProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.List;

/**
 * 判断当前应用是否真的需要接入 XXL 执行器。
 * <p>
 * 只有在组件启用、补偿启用且至少配置了一个 handler 时，
 * 才需要创建或校验 XxlJobExecutor。
 */
public class LocalTaskMessageXxlExecutorRequiredCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        LocalTaskMessageProperties properties = Binder.get(context.getEnvironment())
                .bind("local.task.message", Bindable.of(LocalTaskMessageProperties.class))
                .orElseGet(LocalTaskMessageProperties::new);

        if (!properties.isEnabled() || !properties.getCompensation().isEnabled()) {
            return false;
        }

        LocalTaskMessageProperties.XxlProperties xxlProperties = properties.getXxl();
        if (xxlProperties == null) {
            return false;
        }

        List<LocalTaskMessageProperties.HandlerProperties> handlers = xxlProperties.getHandlers();
        return handlers != null && !handlers.isEmpty();
    }
}
