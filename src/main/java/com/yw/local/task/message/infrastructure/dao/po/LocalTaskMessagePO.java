package com.yw.local.task.message.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 本地任务消息持久化对象。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocalTaskMessagePO {

    /**
     * 自增主键。
     */
    private Long id;

    /**
     * 任务 ID。
     */
    private String taskId;

    /**
     * 任务名称，同时也作为业务名称使用。
     */
    private String taskName;

    /**
     * 通知类型，例如 rabbitmq、http。
     */
    private String notifyType;

    /**
     * 通知配置 JSON。
     */
    private String notifyConfig;

    /**
     * 当前消息状态。
     */
    private Integer status;

    /**
     * 业务参数 JSON。
     */
    private String parameterJson;

    /**
     * 当前消息最终落到哪个门牌号。
     */
    private Integer houseNumber;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;

}
