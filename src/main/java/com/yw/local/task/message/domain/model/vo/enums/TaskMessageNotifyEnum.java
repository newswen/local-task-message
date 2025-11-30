package com.yw.local.task.message.domain.model.vo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @Author: yw
 * @Date: 2025/11/30 18:43
 * @Description:消息回调通知类型：rabbitmq http
 **/
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum TaskMessageNotifyEnum {

    RABBITMQ("rabbitmq", "rabbitmqNotifyStrategy", "rabbitmq"),
    HTTP("http", "httpNotifyStrategy", "http");

    private String code;
    private String strategy;
    private String desc;
}
