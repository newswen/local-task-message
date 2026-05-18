package com.yw.local.task.message.domain.service.notify;

import com.alibaba.fastjson.JSON;
import com.yw.local.task.message.domain.adapter.repository.ILocalTaskMessageRepository;
import com.yw.local.task.message.domain.model.entity.LocalTaskMessageEntityCommand;
import com.yw.local.task.message.domain.service.ILocalTaskMessageNotifyService;
import com.yw.local.task.message.domain.service.notify.strategy.INotifyStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 本地消息通知服务实现。
 * <p>
 * 这里负责两件事：
 * 1. 根据通知类型找到对应策略
 * 2. 在发送结果确定后回写消息状态
 */
@Service
@Slf4j
public class LocalTaskMessageNotifyService implements ILocalTaskMessageNotifyService {

    @Resource
    private Map<String, INotifyStrategy> notifyStrategyMap;

    @Resource
    private ILocalTaskMessageRepository localTaskMessageRepository;

    @Override
    public String notify(LocalTaskMessageEntityCommand event) {
        INotifyStrategy notifyStrategy = resolveNotifyStrategy(event);
        try {
            String result = notifyStrategy.notify(event);
            updateSuccessStatus(event);
            return result;
        } catch (Exception e) {
            updateFailStatus(event, e);
            throw new RuntimeException(e);
        }
    }

    private INotifyStrategy resolveNotifyStrategy(LocalTaskMessageEntityCommand event) {
        if (event == null) {
            throw new IllegalArgumentException("本地消息实体不能为空");
        }
        if (event.getNotifyType() == null) {
            throw new IllegalArgumentException("notifyType 不能为空");
        }

        INotifyStrategy notifyStrategy = notifyStrategyMap.get(event.getNotifyType().getStrategy());
        if (notifyStrategy == null) {
            throw new IllegalArgumentException("未找到通知策略，notifyType=" + event.getNotifyType());
        }
        return notifyStrategy;
    }

    /**
     * 成功状态回写失败时，不应反向把消息打成 FAILED，
     * 否则会把“实际已发送成功”的消息误导入补偿链路。
     */
    private void updateSuccessStatus(LocalTaskMessageEntityCommand event) {
        try {
            localTaskMessageRepository.updateTaskStatusSuccess(event);
        } catch (Exception e) {
            log.error("本地消息发送成功，但状态回写为 COMPLETED 失败，event={}", JSON.toJSONString(event), e);
            throw e;
        }
    }

    /**
     * 发送失败后尝试把消息回写为 FAILED。
     * <p>
     * 如果状态回写也失败，会把该异常附加到原始异常上，便于排查根因。
     */
    private void updateFailStatus(LocalTaskMessageEntityCommand event, Exception cause) {
        try {
            localTaskMessageRepository.updateTaskStatusFail(event);
        } catch (Exception statusException) {
            cause.addSuppressed(statusException);
            log.error("本地消息发送失败，且状态回写为 FAILED 也失败，event={}", JSON.toJSONString(event), statusException);
        }
        log.error("本地消息通知失败，event={}", JSON.toJSONString(event), cause);
    }
}
