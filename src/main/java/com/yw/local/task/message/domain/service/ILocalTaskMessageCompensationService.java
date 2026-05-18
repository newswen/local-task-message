package com.yw.local.task.message.domain.service;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 本地消息补偿服务。
 */
public interface ILocalTaskMessageCompensationService {

    /**
     * 按门牌范围执行失败消息补偿。
     *
     * @param handlerName 当前 XXL handler 名称
     * @param houseNumbers 当前 handler 负责的门牌集合
     * @param maxRetry 最大补偿失败次数
     * @return 本次补偿统计结果
     */
    CompensationResult compensate(String handlerName, List<Integer> houseNumbers, int maxRetry);

    /**
     * 补偿结果统计。
     */
    @Data
    @AllArgsConstructor
    class CompensationResult {
        /**
         * 本次拉取到的消息总数。
         */
        private int total;

        /**
         * 补偿成功数量。
         */
        private int successCount;

        /**
         * 仍然失败数量。
         */
        private int failCount;

        /**
         * 转人工处理数量。
         */
        private int manualCount;
    }
}
