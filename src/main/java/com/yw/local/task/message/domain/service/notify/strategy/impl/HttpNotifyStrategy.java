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
@Service("httpNotifyStrategy")
@Slf4j
public class HttpNotifyStrategy implements INotifyStartegy {

    @Resource
    private ILocalMessagePort httpLocalMessagePort;

    @Override
    public String notify(LocalTaskMessageEntityCommand event) {
        try {
            return httpLocalMessagePort.notify(event);
        } catch (Exception e) {
            log.error("发送http消息失败 error {}", JSON.toJSONString(event));
            throw e;
        }
    }
}
