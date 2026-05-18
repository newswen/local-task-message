package com.yw.local.task.message.infrastructure.support;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 本地消息补偿失败次数计数器。
 * <p>
 * 当前计数只保存在应用进程内存中，不落库。
 * 这意味着应用重启后次数会丢失，这是当前方案明确接受的限制。
 */
@Component
public class LocalTaskMessageRetryCounter {

    /**
     * key：taskId
     * value：当前进程内的补偿失败次数
     */
    private final ConcurrentMap<String, Integer> retryCounter = new ConcurrentHashMap<String, Integer>();

    /**
     * 增加失败次数并返回最新计数。
     */
    public int increment(String taskId) {
        return retryCounter.merge(taskId, 1, Integer::sum);
    }

    /**
     * 获取当前失败次数。
     */
    public int get(String taskId) {
        Integer count = retryCounter.get(taskId);
        return count == null ? 0 : count;
    }

    /**
     * 清理指定任务的失败计数。
     */
    public void clear(String taskId) {
        retryCounter.remove(taskId);
    }
}
