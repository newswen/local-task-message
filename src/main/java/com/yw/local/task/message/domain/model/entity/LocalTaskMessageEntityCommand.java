package com.yw.local.task.message.domain.model.entity;

import com.yw.local.task.message.domain.model.vo.enums.TaskMessageNotifyEnum;
import com.yw.local.task.message.domain.model.vo.enums.TaskStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @Author: yw
 * @Date: 2025/11/30 15:50
 * @Description:消息任务本体
 **/
@Data
public class LocalTaskMessageEntityCommand {

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 通知类型；rabbitmq、http
     */
    private TaskMessageNotifyEnum notifyType;

    /**
     * 通知配置（JSON格式，包含mqTopic和url等信息）
     */
    private NotifyConfig notifyConfig;

    /**
     * 状态（0-创建，2-已完成，3-失败）
     */
    private TaskStatusEnum status;

    /**
     * 参数JSON
     */
    private String parameterJson;

    /**
     * 门牌号
     */
    private Integer houseNumber;


    /*
     * 通知类型 mq http
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NotifyConfig {

        private MQ mq;

        private HTTP http;

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class MQ {
            private String topic;
            private String exchange;
        }

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class HTTP {
            private String url;
            private String method;
            //包含Content-Type以及授权相关其他请求头
            private Map<String, String> headers;
        }

    }

}
