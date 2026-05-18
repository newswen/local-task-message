# 转储表 local_task_message
# ------------------------------------------------------------

DROP TABLE IF EXISTS `local_task_message`;

CREATE TABLE `local_task_message`
(
    `id`             bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `task_id`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '任务ID',
    `task_name`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务名称',
    `notify_type`    varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '通知类型，支持 rabbitmq、http',
    `notify_config`  json                                                                   DEFAULT NULL COMMENT '通知配置(JSON格式)',
    `status`         int                                                           NOT NULL DEFAULT '0' COMMENT '状态：0-创建，1-完成，2-失败，3-人工处理',
    `parameter_json` json                                                                   DEFAULT NULL COMMENT '参数JSON',
    `house_number`   int                                                           NOT NULL DEFAULT '1' COMMENT '门牌号',
    `create_time`    datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_id` (`task_id`),
    KEY `idx_status` (`status`),
    KEY `idx_notify_type` (`notify_type`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='任务消息表';

LOCK TABLES `local_task_message` WRITE;
/*!40000 ALTER TABLE `local_task_message`
    DISABLE KEYS */;
