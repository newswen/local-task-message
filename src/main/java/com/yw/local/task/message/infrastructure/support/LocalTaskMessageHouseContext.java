package com.yw.local.task.message.infrastructure.support;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 当前线程的门牌路由上下文。
 * <p>
 * AOP 在方法入口写入，仓储层在同一线程中读取，
 * 这样业务方只需要在方法上声明 {@code houses}，
 * 保存消息时就能拿到正确的门牌范围。
 */
public final class LocalTaskMessageHouseContext {

    private static final ThreadLocal<RouteContext> CONTEXT = new ThreadLocal<RouteContext>();

    private LocalTaskMessageHouseContext() {
    }

    /**
     * 绑定当前线程上下文。
     */
    public static void bind(RouteContext routeContext) {
        CONTEXT.set(routeContext);
    }

    /**
     * 恢复上一层上下文。
     * <p>
     * 用于处理“被注解方法内部再次调用被注解方法”的嵌套场景。
     */
    public static void restore(RouteContext routeContext) {
        if (routeContext == null) {
            CONTEXT.remove();
            return;
        }
        CONTEXT.set(routeContext);
    }

    /**
     * 获取当前线程上下文。
     */
    public static RouteContext get() {
        return CONTEXT.get();
    }

    /**
     * 获取当前线程上下文，不允许为空。
     */
    public static RouteContext getRequired() {
        RouteContext routeContext = CONTEXT.get();
        if (routeContext == null) {
            throw new IllegalStateException("未获取到本地消息门牌上下文，请确认当前方法已正确标注 @LocalTaskMessage");
        }
        return routeContext;
    }

    /**
     * 更新任务名称，便于在日志里保留更多上下文。
     */
    public static void updateTaskName(String taskName) {
        RouteContext routeContext = CONTEXT.get();
        if (routeContext != null) {
            routeContext.setTaskName(taskName);
        }
    }

    /**
     * 清理当前线程上下文。
     */
    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * 门牌路由上下文。
     */
    @Data
    @AllArgsConstructor
    public static class RouteContext {
        /**
         * 业务方法签名，便于定位是哪一个方法声明了当前门牌范围。
         */
        private String methodSignature;

        /**
         * 原始 houses 表达式，日志里优先打印这个值，排查更直观。
         */
        private String housesExpression;

        /**
         * 当前业务方法允许使用的门牌集合。
         */
        private List<Integer> houseNumbers;

        /**
         * 任务名称，同时也作为业务名称展示。
         */
        private String taskName;

        public RouteContext(String methodSignature, String housesExpression, List<Integer> houseNumbers) {
            this.methodSignature = methodSignature;
            this.housesExpression = housesExpression;
            this.houseNumbers = houseNumbers;
        }
    }
}
