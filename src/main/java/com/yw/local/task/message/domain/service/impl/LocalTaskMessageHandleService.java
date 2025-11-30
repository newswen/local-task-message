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
        //todo 先保存到消息任务数据库
        localTaskMessageRepository.saveTaskMessage(command);
        localTaskMessagePublisher.publish(command);
    }
}
