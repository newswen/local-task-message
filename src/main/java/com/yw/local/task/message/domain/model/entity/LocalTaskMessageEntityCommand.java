package com.yw.local.task.message.domain.model.entity;

import com.yw.local.task.message.domain.model.vo.enums.TaskMessageNotifyEnum;
import com.yw.local.task.message.domain.model.vo.enums.TaskStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 本地任务消息实体。
 */
@Data
public class LocalTaskMessageEntityCommand {

    /**
     * 任务 ID。
     */
    private String taskId;

    /**
     * 任务名称。
     * <p>
     * 当前方案中同时作为业务名称使用。
     */
    private String taskName;

    /**
     * 通知类型。
     */
    private TaskMessageNotifyEnum notifyType;

    /**
     * 通知配置。
     */
    private NotifyConfig notifyConfig;

    /**
     * 当前消息状态。
     */
    private TaskStatusEnum status;

    /**
     * 业务参数 JSON。
     */
    private String parameterJson;

    /**
     * 门牌号。
     * <p>
     * 如果方法上的 houses 只配置了一个门牌，组件可自动补齐；
     * 如果配置了多个门牌，则必须由业务显式指定。
     */
    private Integer houseNumber;

    /**
     * 通知配置，按具体类型承载 MQ / HTTP 的细节。
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NotifyConfig {

        /**
         * RabbitMQ 配置。
         */
        private MQ mq;

        /**
         * HTTP 配置。
         */
        private HTTP http;

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class MQ {
            /**
             * 路由键。
             */
            private String topic;

            /**
             * 交换机名称。
             */
            private String exchange;
        }

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class HTTP {
            /**
             * 回调地址。
             */
            private String url;

            /**
             * 请求方法，例如 GET / POST。
             */
            private String method;

            /**
             * 请求头集合，包含 Content-Type 和鉴权信息等。
             */
            private Map<String, String> headers;
        }

    }

}
