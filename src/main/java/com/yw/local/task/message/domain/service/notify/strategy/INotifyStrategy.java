package com.yw.local.task.message.domain.service.notify.strategy;

import com.yw.local.task.message.domain.model.entity.LocalTaskMessageEntityCommand;

/**
 * 通知策略接口。
 * <p>
 * 不同通知类型通过不同策略实现，避免把 HTTP / MQ 逻辑堆在同一个服务里。
 */
public interface INotifyStrategy {

    /**
     * 发送通知。
     *
     * @param event 本地消息实体
     * @return 对外通知返回结果
     * @throws Exception 发送失败时抛出异常
     */
    String notify(LocalTaskMessageEntityCommand event) throws Exception;
}
