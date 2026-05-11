package com.yw.local.task.message.domain.service.notify;

import com.alibaba.fastjson2.JSON;
import com.yw.local.task.message.domain.adapter.repository.ILocalTaskMessageRepository;
import com.yw.local.task.message.domain.model.entity.LocalTaskMessageEntityCommand;
import com.yw.local.task.message.domain.service.ILocalTaskMessageNotifyService;
import com.yw.local.task.message.domain.service.notify.strategy.INotifyStartegy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 说明
 *
 * @author: yuanwen
 * @since: 2026/5/10
 */
@Service
@Slf4j
public class LocalTaskMessageNotifyService implements ILocalTaskMessageNotifyService {

    @Resource
    private Map<String, INotifyStartegy> notifyStartegyMap;

    @Resource
    private ILocalTaskMessageRepository localTaskMessageRepository;

    @Override
    public String notify(LocalTaskMessageEntityCommand event) {
        try {
            //1.完成MQ&Http消息发送
            INotifyStartegy notifyStartegy = notifyStartegyMap.get(event.getNotifyType().getStrategy());
            //可以为字符串内容或者http响应的json内容
            String result = notifyStartegy.notify(event);
            event.setCallbackResult(result);
            //获取回调消息，存入数据库并更新任务状态为成功
            localTaskMessageRepository.updateTaskStatusSuccess(event);
            return result;
        } catch (Exception e) {
            log.error("处理任务消息事件失败 - error: {}", JSON.toJSONString(event), e);
            //更新任务状态为失败
            localTaskMessageRepository.updateTaskStatusFail(event);
            throw e;
        }
    }
}
