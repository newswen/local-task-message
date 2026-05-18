package com.yw.local.task.message.infrastructure.adapter.entity;

import com.yw.local.task.message.domain.model.entity.LocalTaskMessageEntityCommand;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 本地消息 Spring 事件。
 * <p>
 * 事件体中直接携带业务侧组装好的消息实体，
 * 供异步监听器继续执行即时通知。
 */
@Getter
public class LocalTaskMessageEvent extends ApplicationEvent {

    /**
     * 本地消息实体。
     */
    private final LocalTaskMessageEntityCommand localTaskMessageEntityCommand;

    public LocalTaskMessageEvent(Object source, LocalTaskMessageEntityCommand localTaskMessageEntityCommand) {
        super(source);
        this.localTaskMessageEntityCommand = localTaskMessageEntityCommand;
    }
}
