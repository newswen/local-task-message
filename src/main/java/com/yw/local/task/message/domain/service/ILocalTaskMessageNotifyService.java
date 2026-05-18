package com.yw.local.task.message.domain.service;

import com.yw.local.task.message.domain.model.entity.LocalTaskMessageEntityCommand;

/**
 * 本地消息通知服务。
 * <p>
 * 根据消息中的通知类型和通知配置，执行真正的对外发送动作。
 */
public interface ILocalTaskMessageNotifyService {

    /**
     * 执行消息通知。
     *
     * @param event 本地消息实体
     * @return 对外通知返回值
     */
    String notify(LocalTaskMessageEntityCommand event);
}
