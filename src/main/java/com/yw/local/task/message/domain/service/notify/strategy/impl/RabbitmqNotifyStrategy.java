package com.yw.local.task.message.domain.service.notify.strategy.impl;

import com.alibaba.fastjson2.JSON;
import com.yw.local.task.message.domain.adapter.port.ILocalMessagePort;
import com.yw.local.task.message.domain.model.entity.LocalTaskMessageEntityCommand;
import com.yw.local.task.message.domain.service.notify.strategy.INotifyStartegy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 说明
 *
 * @author: yuanwen
 * @since: 2026/5/10
 */
@Service("rabbitmqNotifyStrategy")
@Slf4j
public class RabbitmqNotifyStrategy implements INotifyStartegy {

    @Resource
    private ILocalMessagePort mqLocalMessagePort;

    @Override
    public String notify(LocalTaskMessageEntityCommand event) {
        try {
            return mqLocalMessagePort.notify(event);
        } catch (Exception e) {
            log.error("发送mq消息失败 error {}", JSON.toJSONString(event));
            throw e;
        }
    }
}
