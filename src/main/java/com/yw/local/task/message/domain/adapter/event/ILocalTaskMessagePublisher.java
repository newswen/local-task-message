package com.yw.local.task.message.domain.adapter.event;

import com.yw.local.task.message.domain.model.entity.LocalTaskMessageEntityCommand;

/**
 * @Author: yw
 * @Date: 2025/11/30 15:59
 * @Description:Spring Event事件发送接口 由基座进行实现
 **/
public interface ILocalTaskMessagePublisher {
    /**
     * 发送事件
     *
     * @param event
     */
    void publish(LocalTaskMessageEntityCommand event);
}
