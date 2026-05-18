package com.yw.local.task.message.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 本地任务消息组件配置。
 */
@Data
@ConfigurationProperties(prefix = "local.task.message")
public class LocalTaskMessageProperties {

    /**
     * 是否启用组件。
     */
    private boolean enabled = true;

    /**
     * 全局门牌配置。
     */
    private HouseProperties house = new HouseProperties();

    /**
     * 补偿相关通用配置。
     */
    private CompensationProperties compensation = new CompensationProperties();

    /**
     * XXL-Job 补偿任务配置。
     */
    private XxlProperties xxl = new XxlProperties();

    @Data
    public static class HouseProperties {
        /**
         * 全局门牌总数。
         * <p>
         * 合法门牌范围为 {@code 0 ~ totalCount - 1}。
         */
        private int totalCount = 10;
    }

    @Data
    public static class CompensationProperties {
        /**
         * 是否启用失败补偿。
         */
        private boolean enabled = true;

        /**
         * 单次补偿扫描批量大小。
         */
        private int batchSize = 100;

        /**
         * 默认最大补偿失败次数。
         * <p>
         * 这里只统计 XXL 补偿失败次数，不包含首次即时发送失败。
         */
        private int maxRetry = 5;

        /**
         * 补偿线程池大小。
         */
        private int threadPoolSize = 8;
    }

    @Data
    public static class XxlProperties {
        /**
         * XXL-Job handler 列表。
         */
        private List<HandlerProperties> handlers = new ArrayList<HandlerProperties>();
    }

    @Data
    public static class HandlerProperties {
        /**
         * XXL-Job handler 名称。
         */
        private String name;

        /**
         * 当前 handler 负责扫描的门牌范围。
         */
        private String houses;

        /**
         * 当前 handler 最大补偿失败次数。
         * <p>
         * 如果不配置，则使用 {@code compensation.max-retry}。
         */
        private Integer maxRetry;
    }
}
