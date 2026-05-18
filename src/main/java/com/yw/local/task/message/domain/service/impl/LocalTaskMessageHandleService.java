package com.yw.local.task.message.domain.service.impl;

import com.yw.local.task.message.domain.adapter.event.ILocalTaskMessagePublisher;
import com.yw.local.task.message.domain.adapter.repository.ILocalTaskMessageRepository;
import com.yw.local.task.message.domain.model.entity.LocalTaskMessageEntityCommand;
import com.yw.local.task.message.domain.service.ILocalTaskMessageHandleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 本地任务消息入口服务实现。
 * <p>
 * 处理顺序固定为：
 * 1. 落库
 * 2. 发布异步事件
 */
@Service
@Slf4j
public class LocalTaskMessageHandleService implements ILocalTaskMessageHandleService {

    @Resource
    private ILocalTaskMessagePublisher localTaskMessagePublisher;

    @Resource
    private ILocalTaskMessageRepository localTaskMessageRepository;

    @Override
    public void handleLocalTaskMessage(LocalTaskMessageEntityCommand command) {
        try {
            localTaskMessageRepository.saveTaskMessage(command);
            localTaskMessagePublisher.publish(command);
        } catch (Exception e) {
            log.error("本地消息组件处理失败，taskId={}，taskName={}", command.getTaskId(), command.getTaskName(), e);
            throw new RuntimeException(e);
        }
    }
}
