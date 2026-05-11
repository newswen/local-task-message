package com.yw.local.task.message.infrastructure.adapter.port;

import com.yw.local.task.message.domain.adapter.port.ILocalMessagePort;
import com.yw.local.task.message.domain.model.entity.LocalTaskMessageEntityCommand;
import com.yw.local.task.message.infrastructure.event.RabbitMQEvent;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 说明
 *
 * @author: yuanwen
 * @since: 2026/5/10
 */
@Service("mqLocalMessagePort")
public class MQLocalMessagePort implements ILocalMessagePort {

    @Resource
    private RabbitMQEvent rabbitMQEvent;

    @Override
    public String notify(LocalTaskMessageEntityCommand event) {
        LocalTaskMessageEntityCommand.NotifyConfig.MQ mq = event.getNotifyConfig().getMq();
        rabbitMQEvent.publish(mq.getExchange(), mq.getTopic(), event.getParameterJson());
        return "success";
    }

}

