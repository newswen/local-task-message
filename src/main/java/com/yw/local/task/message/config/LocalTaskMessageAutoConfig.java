package com.yw.local.task.message.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @Author: yw
 * @Date: 2025/11/30 15:20
 * @Description:整个本地消息组件入口 spring扫描配置
 **/
@Configuration
@ComponentScan("com.yw.local.task.message")
@EnableAsync
public class LocalTaskMessageAutoConfig {

}
