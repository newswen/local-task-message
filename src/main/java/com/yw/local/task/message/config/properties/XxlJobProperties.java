package com.yw.local.task.message.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * XXL-Job 执行器配置。
 * <p>
 * 这些配置由宿主项目在 application.yml / application.properties 中提供，
 * 组件只负责读取并自动装配，不会写死任何 admin 地址。
 */
@Data
@ConfigurationProperties(prefix = "xxl.job")
public class XxlJobProperties {

    /**
     * 官方文档中的 accessToken 主配置。
     */
    private String accessToken;

    /**
     * admin 端配置。
     */
    private AdminProperties admin = new AdminProperties();

    /**
     * executor 端配置。
     */
    private ExecutorProperties executor = new ExecutorProperties();

    /**
     * 兼容两种 accessToken 写法，优先读取 xxl.job.accessToken。
     */
    public String resolveAccessToken() {
        if (StringUtils.hasText(accessToken)) {
            return accessToken;
        }
        return admin.getAccessToken();
    }

    @Data
    public static class AdminProperties {
        /**
         * XXL-Job admin 地址，多个地址使用逗号分隔。
         */
        private String addresses;

        /**
         * 样例配置中的兼容 accessToken 写法。
         */
        private String accessToken;
    }

    @Data
    public static class ExecutorProperties {
        /**
         * 是否启用组件提供的 executor 自动装配。
         */
        private boolean enabled = true;

        /**
         * 执行器 appname。
         */
        private String appname;

        /**
         * 执行器注册地址，通常留空由 XXL 自动推断。
         */
        private String address;

        /**
         * 执行器 IP。
         */
        private String ip;

        /**
         * 执行器端口。
         */
        private int port = 9999;

        /**
         * XXL 日志目录。
         */
        private String logpath;

        /**
         * XXL 日志保留天数。
         */
        private int logretentiondays = 30;
    }
}
