package com.yw.local.task.message.domain.service.impl;

import com.yw.local.task.message.domain.adapter.event.ILocalTaskMessagePublisher;
import com.yw.local.task.message.domain.adapter.repository.ILocalTaskMessageRepository;
import com.yw.local.task.message.domain.model.entity.LocalTaskMessageEntityCommand;
import com.yw.local.task.message.domain.service.ILocalTaskMessageHandleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Author: yw
 * @Date: 2025/11/30 16:15
 * @Description:提供给外部调用的本地消息组件服务实现
 **/
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
            //1.保存当前发送任务消息
            localTaskMessageRepository.saveTaskMessage(command);
            //2.发送消息
            localTaskMessagePublisher.publish(command);
        } catch (Exception e) {
            log.error("本地消息组件处理任务失败：{}", command, e);
            throw new RuntimeException(e);
        }
    }
}
