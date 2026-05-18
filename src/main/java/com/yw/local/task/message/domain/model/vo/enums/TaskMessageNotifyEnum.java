package com.yw.local.task.message.domain.model.vo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息通知类型枚举。
 */
@Getter
@AllArgsConstructor
public enum TaskMessageNotifyEnum {

    /**
     * RabbitMQ 通知。
     */
    RABBITMQ("rabbitmq", "rabbitmqNotifyStrategy", "rabbitmq配置"),

    /**
     * HTTP 回调通知。
     */
    HTTP("http", "httpNotifyStrategy", "http配置");

    /**
     * 存库编码。
     */
    private final String code;

    /**
     * Spring 容器中的策略 Bean 名称。
     */
    private final String strategy;

    /**
     * 类型描述。
     */
    private final String desc;

    public static TaskMessageNotifyEnum fromCode(String code) {
        for (TaskMessageNotifyEnum notifyEnum : TaskMessageNotifyEnum.values()) {
            if (notifyEnum.getCode().equals(code)) {
                return notifyEnum;
            }
        }
        throw new IllegalArgumentException("未知通知类型：" + code);
    }
}
