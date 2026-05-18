package com.yw.local.task.message.domain.service.notify.strategy.impl;

import com.yw.local.task.message.domain.model.entity.LocalTaskMessageEntityCommand;
import com.yw.local.task.message.domain.service.notify.strategy.INotifyStrategy;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * RabbitMQ 通知策略。
 */
@Service("rabbitmqNotifyStrategy")
public class RabbitmqNotifyStrategy implements INotifyStrategy {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Override
    public String notify(LocalTaskMessageEntityCommand event) {
        LocalTaskMessageEntityCommand.NotifyConfig.MQ mq = event.getNotifyConfig() == null ? null : event.getNotifyConfig().getMq();
        if (mq == null || mq.getExchange() == null || mq.getExchange().trim().isEmpty()) {
            throw new IllegalArgumentException("RabbitMQ 通知配置不能为空");
        }

        String routingKey = mq.getTopic() == null ? "" : mq.getTopic();
        rabbitTemplate.convertAndSend(mq.getExchange(), routingKey, event.getParameterJson());
        return "OK";
    }
}
