package com.yw.local.task.message.domain.adapter.event;

import com.yw.local.task.message.domain.model.entity.LocalTaskMessageEntityCommand;

/**
 * 本地消息事件发布接口。
 * <p>
 * 当前由 Spring ApplicationEvent 实现，
 * 后续如果要替换为别的进程内事件机制，也只需要改适配层。
 */
public interface ILocalTaskMessagePublisher {

    /**
     * 发布本地消息事件。
     *
     * @param event 本地消息实体
     */
    void publish(LocalTaskMessageEntityCommand event);
}
