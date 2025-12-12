package com.yw.local.task.message.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * @Author: yw
 * @Date: 2025/11/30 18:41
 * @Description:本地消息实体
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocalTaskMessage {

    /**
     * 自增主键
     */
    private Long id;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 通知类型；rabbitmq、http
     */
    private String notifyType;

    /**
     * 通知配置（JSON格式，包含mqTopic和url等信息）
     */
    private String notifyConfig;

    /**
     * 状态（0-待处理，1-处理中，2-已完成，3-失败）
     */
    private Integer status;

    /**
     * 参数JSON
     */
    private String parameterJson;

    /**
     * 门牌号
     */
    private Integer houseNumber;

    /**
     * 创建时间
     */
    private LocalDate createTime;

    /**
     * 更新时间
     */
    private LocalDate updateTime;

}
