package com.yw.local.task.message.domain.service.compensation;

import com.yw.local.task.message.config.properties.LocalTaskMessageProperties;
import com.yw.local.task.message.domain.adapter.repository.ILocalTaskMessageRepository;
import com.yw.local.task.message.domain.model.entity.LocalTaskMessageEntityCommand;
import com.yw.local.task.message.domain.service.ILocalTaskMessageCompensationService;
import com.yw.local.task.message.domain.service.ILocalTaskMessageNotifyService;
import com.yw.local.task.message.infrastructure.support.LocalTaskMessageRetryCounter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 本地消息补偿服务实现。
 * <p>
 * 负责查询指定门牌范围内的失败消息，并发执行补偿并统计结果。
 */
@Service
@Slf4j
public class LocalTaskMessageCompensationService implements ILocalTaskMessageCompensationService {

    @Resource
    private LocalTaskMessageProperties localTaskMessageProperties;

    @Resource
    private ILocalTaskMessageRepository localTaskMessageRepository;

    @Resource
    private ILocalTaskMessageNotifyService localTaskMessageNotifyService;

    @Resource
    private LocalTaskMessageRetryCounter retryCounter;

    private ThreadPoolTaskExecutor executor;

    @PostConstruct
    public void init() {
        int threadPoolSize = localTaskMessageProperties.getCompensation().getThreadPoolSize();
        if (threadPoolSize <= 0) {
            throw new IllegalArgumentException("local.task.message.compensation.thread-pool-size 必须大于 0");
        }

        executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("local-task-message-compensation-");
        executor.setCorePoolSize(threadPoolSize);
        executor.setMaxPoolSize(threadPoolSize);
        executor.setQueueCapacity(localTaskMessageProperties.getCompensation().getBatchSize());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
    }

    @PreDestroy
    public void destroy() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    @Override
    public CompensationResult compensate(String handlerName, List<Integer> houseNumbers, int maxRetry) {
        if (!localTaskMessageProperties.isEnabled() || !localTaskMessageProperties.getCompensation().isEnabled()) {
            log.info("本地消息补偿已关闭，handlerName={}", handlerName);
            return new CompensationResult(0, 0, 0, 0);
        }
        if (maxRetry <= 0) {
            throw new IllegalArgumentException("maxRetry 必须大于 0，handlerName=" + handlerName);
        }

        List<LocalTaskMessageEntityCommand> messages = localTaskMessageRepository.queryCompensationMessages(
                houseNumbers,
                localTaskMessageProperties.getCompensation().getBatchSize()
        );
        if (messages.isEmpty()) {
            return new CompensationResult(0, 0, 0, 0);
        }

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();
        AtomicInteger manualCount = new AtomicInteger();
        CompletableFuture<?>[] futures = new CompletableFuture[messages.size()];

        for (int i = 0; i < messages.size(); i++) {
            final LocalTaskMessageEntityCommand message = messages.get(i);
            futures[i] = CompletableFuture.runAsync(new Runnable() {
                @Override
                public void run() {
                    compensateSingleMessage(handlerName, message, maxRetry, successCount, failCount, manualCount);
                }
            }, executor);
        }

        CompletableFuture.allOf(futures).join();
        return new CompensationResult(messages.size(), successCount.get(), failCount.get(), manualCount.get());
    }

    /**
     * 单条补偿逻辑。
     * <p>
     * 成功则清空重试计数；失败则累加计数并判断是否进入人工处理。
     */
    private void compensateSingleMessage(String handlerName,
                                         LocalTaskMessageEntityCommand message,
                                         int maxRetry,
                                         AtomicInteger successCount,
                                         AtomicInteger failCount,
                                         AtomicInteger manualCount) {
        try {
            localTaskMessageNotifyService.notify(message);
            retryCounter.clear(message.getTaskId());
            successCount.incrementAndGet();
        } catch (Exception e) {
            int currentRetry = retryCounter.increment(message.getTaskId());
            if (currentRetry >= maxRetry) {
                localTaskMessageRepository.updateTaskStatusManualProcessing(message);
                retryCounter.clear(message.getTaskId());
                manualCount.incrementAndGet();
                log.warn("本地消息补偿超过最大重试次数，转人工处理，handlerName={}，taskId={}，maxRetry={}",
                        handlerName, message.getTaskId(), maxRetry);
                return;
            }

            failCount.incrementAndGet();
            log.error("本地消息补偿失败，handlerName={}，taskId={}，currentRetry={}，maxRetry={}",
                    handlerName, message.getTaskId(), currentRetry, maxRetry, e);
        }
    }
}
