package com.yw.local.task.message;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 本地任务消息注解。
 * <p>
 * 标在业务方法上后，组件会在方法执行完成后提取消息实体、
 * 保存本地消息并触发异步通知。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface LocalTaskMessage {

    /**
     * 消息实体属性路径。
     * <p>
     * 当 {@code LocalTaskMessageEntityCommand} 不是方法直接参数，
     * 而是嵌套在某个 DTO 字段中时，可以通过这个属性路径告诉组件去哪里取值。
     * 例如：{@code request.messageCommand}
     */
    String entityAttributeName() default "";

    /**
     * 当前业务方法允许使用的门牌范围。
     * <p>
     * 支持三种写法：
     * 1. 单个门牌：{@code 1}
     * 2. 连续范围：{@code 0-9}
     * 3. 混合范围：{@code 0,2,5-7}
     * <p>
     * 这里配置的是“该业务方法落库时允许使用的门牌范围”，
     * 不是 XXL-Job 的扫描配置。
     */
    String houses();

}
