package com.yw.local.task.message.domain.model.entity;

import lombok.Data;

/**
 * @Author: yw
 * @Date: 2025/11/30 15:50
 * @Description:消息任务本体
 **/
@Data
public class LocalTaskMessageEntityCommand {

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 任务名称
     */
    private String taskName;

}
