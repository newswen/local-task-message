package com.yw.local.task.message.trigger.listener;

import com.yw.local.task.message.infrastructure.adapter.entity.LocalTaskMessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @Author: yw
 * @Date: 2025/11/30 16:04
 * @Description:
 **/
@Service
@Slf4j
public class LocalTaskMessageListener {

    @EventListener
    @Async
    public void handleLocalTaskMessageEvent(LocalTaskMessageEvent event) {
        try {
            log.info("收到任务消息事件 - 消息内容: {}, 事件时间戳: {}", event.getLocalTaskMessageEntityCommand(), event.getTimestamp());

        } catch (Exception e) {
            log.error("处理任务消息事件失败 - 消息: {}, 错误: {}",
                    event.getLocalTaskMessageEntityCommand(), e.getMessage(), e);
        }
    }

}
