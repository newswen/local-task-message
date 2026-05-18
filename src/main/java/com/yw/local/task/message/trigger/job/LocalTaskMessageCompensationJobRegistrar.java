package com.yw.local.task.message.trigger.job;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.handler.IJobHandler;
import com.yw.local.task.message.config.properties.LocalTaskMessageProperties;
import com.yw.local.task.message.domain.service.ILocalTaskMessageCompensationService;
import com.yw.local.task.message.infrastructure.support.HouseNumberRangeParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * XXL-Job 补偿任务注册器。
 * <p>
 * 启动时会校验 handler 名称和门牌范围，避免重复注册或扫描范围冲突。
 */
@Component
@Slf4j
public class LocalTaskMessageCompensationJobRegistrar implements SmartInitializingSingleton {

    @Resource
    private LocalTaskMessageProperties localTaskMessageProperties;

    @Resource
    private ILocalTaskMessageCompensationService compensationService;

    @Override
    public void afterSingletonsInstantiated() {
        if (!localTaskMessageProperties.isEnabled() || !localTaskMessageProperties.getCompensation().isEnabled()) {
            log.info("跳过本地消息补偿任务注册，补偿功能未开启");
            return;
        }

        List<LocalTaskMessageProperties.HandlerProperties> handlers = localTaskMessageProperties.getXxl().getHandlers();
        if (handlers == null || handlers.isEmpty()) {
            log.info("跳过本地消息补偿任务注册，未配置 handler");
            return;
        }

        validateHandlers(handlers);
        for (LocalTaskMessageProperties.HandlerProperties handler : handlers) {
            List<Integer> houseNumbers = HouseNumberRangeParser.parse(
                    handler.getHouses(),
                    localTaskMessageProperties.getHouse().getTotalCount()
            );
            int maxRetry = resolveMaxRetry(handler);
            XxlJobExecutor.registJobHandler(handler.getName(), new LocalTaskMessageCompensationJobHandler(
                    handler.getName(),
                    houseNumbers,
                    maxRetry,
                    compensationService
            ));
            log.info("注册本地消息补偿 handler 成功，name={}，houses={}，maxRetry={}", handler.getName(), houseNumbers, maxRetry);
        }
    }

    private void validateHandlers(List<LocalTaskMessageProperties.HandlerProperties> handlers) {
        Map<Integer, String> ownerByHouse = new HashMap<Integer, String>();
        Set<String> handlerNames = new HashSet<String>();
        for (LocalTaskMessageProperties.HandlerProperties handler : handlers) {
            if (handler.getName() == null || handler.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("XXL handler 名称不能为空");
            }
            if (!handlerNames.add(handler.getName())) {
                throw new IllegalStateException("XXL handler 名称重复：" + handler.getName());
            }

            List<Integer> houseNumbers = HouseNumberRangeParser.parse(
                    handler.getHouses(),
                    localTaskMessageProperties.getHouse().getTotalCount()
            );
            for (Integer houseNumber : houseNumbers) {
                String existingHandler = ownerByHouse.putIfAbsent(houseNumber, handler.getName());
                if (existingHandler != null) {
                    throw new IllegalStateException("XXL handler 门牌范围重叠，houseNumber=" + houseNumber
                            + "，existingHandler=" + existingHandler + "，currentHandler=" + handler.getName());
                }
            }
            resolveMaxRetry(handler);
        }
    }

    private int resolveMaxRetry(LocalTaskMessageProperties.HandlerProperties handler) {
        int maxRetry = handler.getMaxRetry() == null
                ? localTaskMessageProperties.getCompensation().getMaxRetry()
                : handler.getMaxRetry();
        if (maxRetry <= 0) {
            throw new IllegalArgumentException("handler 的 maxRetry 必须大于 0，handlerName=" + handler.getName());
        }
        return maxRetry;
    }

    /**
     * XXL-Job 动态注册 handler。
     */
    private static class LocalTaskMessageCompensationJobHandler extends IJobHandler {

        private final String handlerName;
        private final List<Integer> houseNumbers;
        private final int maxRetry;
        private final ILocalTaskMessageCompensationService compensationService;

        private LocalTaskMessageCompensationJobHandler(String handlerName,
                                                       List<Integer> houseNumbers,
                                                       int maxRetry,
                                                       ILocalTaskMessageCompensationService compensationService) {
            this.handlerName = handlerName;
            this.houseNumbers = houseNumbers;
            this.maxRetry = maxRetry;
            this.compensationService = compensationService;
        }

        @Override
        public void execute() {
            ILocalTaskMessageCompensationService.CompensationResult result =
                    compensationService.compensate(handlerName, houseNumbers, maxRetry);
            XxlJobHelper.log("local task message compensation finished, handlerName:{0}, total:{1}, success:{2}, fail:{3}, manual:{4}",
                    handlerName, result.getTotal(), result.getSuccessCount(), result.getFailCount(), result.getManualCount());
        }
    }
}
