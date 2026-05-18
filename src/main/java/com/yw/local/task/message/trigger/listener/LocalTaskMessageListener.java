package com.yw.local.task.message.trigger.listener;

import com.yw.local.task.message.domain.service.ILocalTaskMessageNotifyService;
import com.yw.local.task.message.infrastructure.adapter.entity.LocalTaskMessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 本地消息异步监听器。
 * <p>
 * 监听器只负责“即时发送”这条链路。
 * 如果这里发送失败，状态会被通知服务改为 FAILED，
 * 后续再由 XXL-Job 做补偿。
 */
@Service
@Slf4j
public class LocalTaskMessageListener {

    @Resource
    private ILocalTaskMessageNotifyService localTaskMessageNotifyService;

    @EventListener
    @Async
    public void handleLocalTaskMessageEvent(LocalTaskMessageEvent event) {
        try {
            log.info("收到本地任务消息事件，taskId={}，taskName={}",
                    event.getLocalTaskMessageEntityCommand().getTaskId(),
                    event.getLocalTaskMessageEntityCommand().getTaskName());
            localTaskMessageNotifyService.notify(event.getLocalTaskMessageEntityCommand());
        } catch (Exception e) {
            log.error("处理本地任务消息事件失败，taskId={}",
                    event.getLocalTaskMessageEntityCommand().getTaskId(), e);
        }
    }
}
