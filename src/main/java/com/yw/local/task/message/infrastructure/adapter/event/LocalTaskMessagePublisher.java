package com.yw.local.task.message.infrastructure.adapter.event;

import com.yw.local.task.message.domain.adapter.event.ILocalTaskMessagePublisher;
import com.yw.local.task.message.domain.model.entity.LocalTaskMessageEntityCommand;
import com.yw.local.task.message.infrastructure.adapter.entity.LocalTaskMessageEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 本地消息事件发布实现。
 */
@Service
public class LocalTaskMessagePublisher implements ILocalTaskMessagePublisher {

    @Resource
    private ApplicationEventPublisher publisher;

    @Override
    public void publish(LocalTaskMessageEntityCommand event) {
        LocalTaskMessageEvent localTaskMessageEvent = new LocalTaskMessageEvent(this, event);
        publisher.publishEvent(localTaskMessageEvent);
    }
}
