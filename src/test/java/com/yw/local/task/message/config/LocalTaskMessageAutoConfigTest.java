package com.yw.local.task.message.config;

import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import com.yw.local.task.message.config.properties.XxlJobProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

class LocalTaskMessageAutoConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LocalTaskMessageAutoConfig.class))
            .withBean(DataSource.class, () -> Mockito.mock(DataSource.class))
            .withBean(RabbitTemplate.class, () -> Mockito.mock(RabbitTemplate.class))
            .withBean(TransactionTemplate.class, () -> Mockito.mock(TransactionTemplate.class));

    @Test
    void shouldCreateExecutorFromXxlProperties() {
        LocalTaskMessageAutoConfig.XxlJobExecutorAutoConfiguration autoConfig =
                new LocalTaskMessageAutoConfig.XxlJobExecutorAutoConfiguration();
        XxlJobProperties xxlJobProperties = createCompleteXxlJobProperties();

        XxlJobSpringExecutor executor = autoConfig.localTaskMessageXxlJobExecutor(xxlJobProperties);

        Assertions.assertEquals("http://127.0.0.1:8080/xxl-job-admin/", ReflectionTestUtils.getField(executor, "adminAddresses"));
        Assertions.assertEquals("primary-token", ReflectionTestUtils.getField(executor, "accessToken"));
        Assertions.assertEquals("demo-executor", ReflectionTestUtils.getField(executor, "appname"));
        Assertions.assertEquals("http://127.0.0.1:9999/", ReflectionTestUtils.getField(executor, "address"));
        Assertions.assertEquals("127.0.0.1", ReflectionTestUtils.getField(executor, "ip"));
        Assertions.assertEquals(9999, ReflectionTestUtils.getField(executor, "port"));
        Assertions.assertEquals("D:/data/applogs/xxl-job/jobhandler", ReflectionTestUtils.getField(executor, "logPath"));
        Assertions.assertEquals(30, ReflectionTestUtils.getField(executor, "logRetentionDays"));
    }

    @Test
    void shouldFallbackToAdminAccessTokenWhenRootAccessTokenMissing() {
        LocalTaskMessageAutoConfig.XxlJobExecutorAutoConfiguration autoConfig =
                new LocalTaskMessageAutoConfig.XxlJobExecutorAutoConfiguration();
        XxlJobProperties xxlJobProperties = createCompleteXxlJobProperties();
        xxlJobProperties.setAccessToken(null);
        xxlJobProperties.getAdmin().setAccessToken("admin-fallback-token");

        XxlJobSpringExecutor executor = autoConfig.localTaskMessageXxlJobExecutor(xxlJobProperties);

        Assertions.assertEquals("admin-fallback-token", ReflectionTestUtils.getField(executor, "accessToken"));
    }

    @Test
    void shouldNotRequireXxlPropertiesWhenCompensationDisabled() {
        contextRunner
                .withPropertyValues(
                        "local.task.message.enabled=true",
                        "local.task.message.compensation.enabled=false"
                )
                .run(context -> {
                    Assertions.assertNull(context.getStartupFailure());
                    Assertions.assertTrue(context.getBeansOfType(XxlJobExecutor.class).isEmpty());
                });
    }

    @Test
    void shouldNotRequireXxlPropertiesWhenNoHandlersConfigured() {
        contextRunner
                .withPropertyValues(
                        "local.task.message.enabled=true",
                        "local.task.message.compensation.enabled=true"
                )
                .run(context -> {
                    Assertions.assertNull(context.getStartupFailure());
                    Assertions.assertTrue(context.getBeansOfType(XxlJobExecutor.class).isEmpty());
                });
    }

    @Test
    void shouldFailWhenAdminAddressesMissing() {
        contextRunner
                .withPropertyValues(
                        "local.task.message.enabled=true",
                        "local.task.message.compensation.enabled=true",
                        "local.task.message.xxl.handlers[0].name=localMessageCompensateA",
                        "local.task.message.xxl.handlers[0].houses=0",
                        "xxl.job.executor.appname=demo-executor",
                        "xxl.job.executor.logpath=D:/data/applogs/xxl-job/jobhandler"
                )
                .run(context -> assertStartupFailureContains(context, "xxl.job.admin.addresses"));
    }

    @Test
    void shouldFailWhenExecutorAppnameMissing() {
        contextRunner
                .withPropertyValues(
                        "local.task.message.enabled=true",
                        "local.task.message.compensation.enabled=true",
                        "local.task.message.xxl.handlers[0].name=localMessageCompensateA",
                        "local.task.message.xxl.handlers[0].houses=0",
                        "xxl.job.admin.addresses=http://127.0.0.1:8080/xxl-job-admin/",
                        "xxl.job.executor.logpath=D:/data/applogs/xxl-job/jobhandler"
                )
                .run(context -> assertStartupFailureContains(context, "xxl.job.executor.appname"));
    }

    @Test
    void shouldFailWhenExecutorLogPathMissing() {
        contextRunner
                .withPropertyValues(
                        "local.task.message.enabled=true",
                        "local.task.message.compensation.enabled=true",
                        "local.task.message.xxl.handlers[0].name=localMessageCompensateA",
                        "local.task.message.xxl.handlers[0].houses=0",
                        "xxl.job.admin.addresses=http://127.0.0.1:8080/xxl-job-admin/",
                        "xxl.job.executor.appname=demo-executor"
                )
                .run(context -> assertStartupFailureContains(context, "xxl.job.executor.logpath"));
    }

    @Test
    void shouldFailWhenExecutorAutoConfigDisabledWithoutCustomBean() {
        contextRunner
                .withPropertyValues(
                        "local.task.message.enabled=true",
                        "local.task.message.compensation.enabled=true",
                        "local.task.message.xxl.handlers[0].name=localMessageCompensateA",
                        "local.task.message.xxl.handlers[0].houses=0",
                        "xxl.job.executor.enabled=false"
                )
                .run(context -> assertStartupFailureContains(context, "xxl.job.executor.enabled=false"));
    }

    @Test
    void shouldFailWithClearMessageWhenXxlJobCoreMissing() {
        contextRunner
                .withClassLoader(new FilteredClassLoader("com.xxl.job.core"))
                .withPropertyValues(
                        "local.task.message.enabled=true",
                        "local.task.message.compensation.enabled=true",
                        "local.task.message.xxl.handlers[0].name=localMessageCompensateA",
                        "local.task.message.xxl.handlers[0].houses=0",
                        "xxl.job.admin.addresses=http://127.0.0.1:8080/xxl-job-admin/",
                        "xxl.job.executor.appname=demo-executor",
                        "xxl.job.executor.logpath=D:/data/applogs/xxl-job/jobhandler"
                )
                .run(context -> assertStartupFailureContains(context, "com.xuxueli:xxl-job-core"));
    }

    @Test
    void shouldNotCreateDuplicateExecutorWhenHostProvidesExecutorBean() {
        contextRunner
                .withBean(XxlJobExecutor.class, XxlJobExecutor::new)
                .withPropertyValues(
                        "local.task.message.enabled=true",
                        "local.task.message.compensation.enabled=true",
                        "local.task.message.xxl.handlers[0].name=localMessageCompensateA",
                        "local.task.message.xxl.handlers[0].houses=0",
                        "xxl.job.admin.addresses=http://127.0.0.1:8080/xxl-job-admin/",
                        "xxl.job.executor.appname=demo-executor",
                        "xxl.job.executor.logpath=D:/data/applogs/xxl-job/jobhandler"
                )
                .run(context -> {
                    Assertions.assertNull(context.getStartupFailure());
                    Assertions.assertEquals(1, context.getBeansOfType(XxlJobExecutor.class).size());
                    Assertions.assertFalse(context.containsBean("localTaskMessageXxlJobExecutor"));
                });
    }

    @Test
    void shouldAllowDisabledAutoConfigWhenHostProvidesExecutorBean() {
        contextRunner
                .withBean(XxlJobExecutor.class, XxlJobExecutor::new)
                .withPropertyValues(
                        "local.task.message.enabled=true",
                        "local.task.message.compensation.enabled=true",
                        "local.task.message.xxl.handlers[0].name=localMessageCompensateA",
                        "local.task.message.xxl.handlers[0].houses=0",
                        "xxl.job.executor.enabled=false"
                )
                .run(context -> {
                    Assertions.assertNull(context.getStartupFailure());
                    Assertions.assertEquals(1, context.getBeansOfType(XxlJobExecutor.class).size());
                });
    }

    private void assertStartupFailureContains(org.springframework.boot.test.context.assertj.AssertableApplicationContext context,
                                              String expectedText) {
        Throwable startupFailure = context.getStartupFailure();
        Assertions.assertNotNull(startupFailure);
        Assertions.assertTrue(startupFailure.getMessage().contains(expectedText), startupFailure.getMessage());
    }

    private XxlJobProperties createCompleteXxlJobProperties() {
        XxlJobProperties properties = new XxlJobProperties();
        properties.getAdmin().setAddresses("http://127.0.0.1:8080/xxl-job-admin/");
        properties.setAccessToken("primary-token");
        properties.getExecutor().setAppname("demo-executor");
        properties.getExecutor().setAddress("http://127.0.0.1:9999/");
        properties.getExecutor().setIp("127.0.0.1");
        properties.getExecutor().setPort(9999);
        properties.getExecutor().setLogpath("D:/data/applogs/xxl-job/jobhandler");
        properties.getExecutor().setLogretentiondays(30);
        return properties;
    }
}
