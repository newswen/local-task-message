package com.yw.local.task.message.domain.service;

import com.yw.local.task.message.domain.model.entity.LocalTaskMessageEntityCommand;

/**
 * 本地任务消息入口服务。
 * <p>
 * 负责接收 AOP 提取出的消息实体，完成落库和事件发布。
 */
public interface ILocalTaskMessageHandleService {

    /**
     * 处理本地消息主流程。
     *
     * @param command 本地消息实体
     */
    void handleLocalTaskMessage(LocalTaskMessageEntityCommand command);

}
