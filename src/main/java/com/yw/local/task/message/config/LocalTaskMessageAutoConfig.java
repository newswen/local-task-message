package com.yw.local.task.message.config;

import com.yw.local.task.message.config.condition.LocalTaskMessageXxlExecutorRequiredCondition;
import com.yw.local.task.message.config.properties.LocalTaskMessageProperties;
import com.yw.local.task.message.config.properties.XxlJobProperties;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 本地任务消息组件自动配置入口。
 */
@Configuration
@ComponentScan("com.yw.local.task.message")
@EnableAsync
@EnableConfigurationProperties({LocalTaskMessageProperties.class, XxlJobProperties.class})
public class LocalTaskMessageAutoConfig {

    @Bean
    @Conditional(LocalTaskMessageXxlExecutorRequiredCondition.class)
    @ConditionalOnProperty(prefix = "xxl.job.executor", name = "enabled", havingValue = "false")
    @ConditionalOnMissingBean(type = "com.xxl.job.core.executor.XxlJobExecutor")
    public SmartInitializingSingleton localTaskMessageXxlExecutorDisabledValidator() {
        return new SmartInitializingSingleton() {
            @Override
            public void afterSingletonsInstantiated() {
                throw new IllegalStateException("本地消息补偿已开启且已配置 handler，但 xxl.job.executor.enabled=false 且未提供 XxlJobExecutor Bean；请在宿主项目配置 xxl.job.* 或自行提供 XxlJobExecutor Bean");
            }
        };
    }

    @Bean
    @Conditional(LocalTaskMessageXxlExecutorRequiredCondition.class)
    @ConditionalOnProperty(prefix = "xxl.job.executor", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingClass("com.xxl.job.core.executor.impl.XxlJobSpringExecutor")
    public SmartInitializingSingleton localTaskMessageMissingXxlJobDependencyValidator() {
        return new SmartInitializingSingleton() {
            @Override
            public void afterSingletonsInstantiated() {
                throw new IllegalStateException("本地消息补偿已开启且已配置 handler，但当前类路径缺少 XXL-Job 执行器依赖；请确认宿主项目已引入并刷新 com.xuxueli:xxl-job-core");
            }
        };
    }

    static void validateRequiredExecutorProperties(XxlJobProperties xxlJobProperties) {
        List<String> missingProperties = new ArrayList<String>();
        if (!StringUtils.hasText(xxlJobProperties.getAdmin().getAddresses())) {
            missingProperties.add("xxl.job.admin.addresses");
        }
        if (!StringUtils.hasText(xxlJobProperties.getExecutor().getAppname())) {
            missingProperties.add("xxl.job.executor.appname");
        }
        if (!StringUtils.hasText(xxlJobProperties.getExecutor().getLogpath())) {
            missingProperties.add("xxl.job.executor.logpath");
        }

        if (!missingProperties.isEmpty()) {
            throw new IllegalStateException("本地消息补偿已开启且已配置 handler，请在宿主项目配置 xxl.job.*，缺少配置：" + String.join(", ", missingProperties));
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "com.xxl.job.core.executor.impl.XxlJobSpringExecutor")
    static class XxlJobExecutorAutoConfiguration {

        @Bean
        @Conditional(LocalTaskMessageXxlExecutorRequiredCondition.class)
        @ConditionalOnProperty(prefix = "xxl.job.executor", name = "enabled", havingValue = "true", matchIfMissing = true)
        @ConditionalOnMissingBean(type = "com.xxl.job.core.executor.XxlJobExecutor")
        public com.xxl.job.core.executor.impl.XxlJobSpringExecutor localTaskMessageXxlJobExecutor(XxlJobProperties xxlJobProperties) {
            LocalTaskMessageAutoConfig.validateRequiredExecutorProperties(xxlJobProperties);

            com.xxl.job.core.executor.impl.XxlJobSpringExecutor executor =
                    new com.xxl.job.core.executor.impl.XxlJobSpringExecutor();
            executor.setAdminAddresses(xxlJobProperties.getAdmin().getAddresses());
            executor.setAccessToken(xxlJobProperties.resolveAccessToken());
            executor.setAppname(xxlJobProperties.getExecutor().getAppname());
            executor.setAddress(xxlJobProperties.getExecutor().getAddress());
            executor.setIp(xxlJobProperties.getExecutor().getIp());
            executor.setPort(xxlJobProperties.getExecutor().getPort());
            executor.setLogPath(xxlJobProperties.getExecutor().getLogpath());
            executor.setLogRetentionDays(xxlJobProperties.getExecutor().getLogretentiondays());
            return executor;
        }
    }
}
