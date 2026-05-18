# 本地任务消息组件使用说明

## 1. 组件作用

这个组件用来实现一条稳定的本地消息链路：

1. 业务方法执行
2. AOP 提取 `LocalTaskMessageEntityCommand`
3. 消息先落库，状态置为 `CREATED`
4. 通过 Spring Event 异步即时发送
5. 发送成功后更新为 `COMPLETED`
6. 发送失败后更新为 `FAILED`
7. XXL-Job 扫描 `FAILED` 消息做补偿
8. 补偿失败次数达到上限后更新为 `MANUAL_PROCESSING`

适用场景：

- 主业务成功后，需要发 RabbitMQ 或 HTTP 通知
- 不希望即时发送失败影响主业务提交
- 希望后续通过 XXL-Job 对失败消息做补偿

## 2. 前置准备

### 2.1 建表

执行 SQL 脚本：

- [local_task_message.sql](../../../../../../doc/dev-ops/mysql/local_task_message.sql)

状态说明：

- `0`：`CREATED`
- `1`：`COMPLETED`
- `2`：`FAILED`
- `3`：`MANUAL_PROCESSING`

### 2.2 宿主应用能力要求

宿主应用需要具备以下基础能力：

- Spring Boot
- `DataSource`
- `TransactionTemplate`
- 如果发送 RabbitMQ，需要 `RabbitTemplate`
- 如果启用失败补偿，需要接入 XXL-Job

## 3. XXL-Job 接入方式

### 3.1 关键原则        

XXL-Job 的连接信息必须由宿主项目自己在配置文件中提供，组件不会写死任何 admin 地址。

组件会自动完成两件事：

- 自动注册本地消息补偿 `handler`
- 在满足条件时自动创建 `XxlJobSpringExecutor`

宿主项目只需要配置 `application.yml` 或 `application.properties`，不需要再手写 `XxlJobSpringExecutor` Bean。

如果宿主项目已经自己提供了 `XxlJobExecutor` / `XxlJobSpringExecutor` Bean，组件会自动让位，不重复创建。

### 3.2 什么时候会自动装配 executor

只有满足下面三个条件时，组件才会自动装配 XXL 执行器：

- `local.task.message.enabled=true`
- `local.task.message.compensation.enabled=true`
- `local.task.message.xxl.handlers` 至少配置了一个 handler

如果补偿关闭，或者根本没有配置 handler，就不会要求 `xxl.job.*` 配置。

### 3.3 必填 XXL 配置

当补偿开启且已经配置 handler 时，宿主项目至少需要提供：

- `xxl.job.admin.addresses`
- `xxl.job.executor.appname`
- `xxl.job.executor.logpath`

缺少任意项时，应用会在启动阶段直接失败，并提示“请在宿主项目配置 `xxl.job.*`”。

### 3.4 XXL 配置示例

下面是一个完整示例，其中 admin 地址只是示例值，实际请由每个宿主项目按自己的环境填写：

```yaml
xxl:
  job:
    admin:
      addresses: http://115.190.194.189:8080/xxl-job-admin/
    access-token: demo-token
    executor:
      enabled: true
      appname: local-task-message-executor
      address:
      ip:
      port: 9999
      logpath: D:/data/applogs/xxl-job/jobhandler
      logretentiondays: 30
```

兼容说明：

- 优先读取 `xxl.job.access-token`
- 如果没有配置主字段，也兼容 `xxl.job.admin.access-token`

## 4. 组件配置

组件配置前缀：

```yaml
local:
  task:
    message:
```

完整示例：

```yaml
local:
  task:
    message:
      enabled: true
      house:
        total-count: 10
      compensation:
        enabled: true
        batch-size: 100
        max-retry: 5
        thread-pool-size: 8
      xxl:
        handlers:
          - name: localMessageCompensateA
            houses: 0
            max-retry: 10
          - name: localMessageCompensateB
            houses: 1-3
          - name: localMessageCompensateC
            houses: 4,5,6-9
```

字段说明：

- `enabled`：是否启用组件
- `house.total-count`：全局门牌总数，合法范围是 `0 ~ total-count - 1`
- `compensation.enabled`：是否启用失败补偿
- `compensation.batch-size`：单次补偿扫描批量大小
- `compensation.max-retry`：默认最大补偿失败次数
- `compensation.thread-pool-size`：补偿线程池大小
- `xxl.handlers[].name`：XXL-Job handler 名称
- `xxl.handlers[].houses`：当前 handler 负责扫描的门牌范围
- `xxl.handlers[].max-retry`：当前 handler 的最大补偿失败次数，未配置时使用全局默认值

注意：

- 方法上的 `houses` 允许重复
- `xxl.handlers[].houses` 之间不允许重叠，启动时会直接报错

## 5. 核心对象说明

### 5.1 `@LocalTaskMessage`

位置：

- [LocalTaskMessage.java](./LocalTaskMessage.java)

字段说明：

- `houses`：当前业务方法允许使用的门牌范围
- `entityAttributeName`：当消息实体不是方法直接参数时，用属性路径提取实体

`houses` 支持：

- 单个门牌：`1`
- 连续范围：`0-9`
- 混合范围：`0,2,5-7`

### 5.2 `LocalTaskMessageEntityCommand`

位置：

- [LocalTaskMessageEntityCommand.java](./domain/model/entity/LocalTaskMessageEntityCommand.java)

关键字段：

- `taskId`：消息唯一 ID，建议业务自行保证唯一
- `taskName`：任务名，同时也作为业务名使用
- `notifyType`：通知类型，支持 `HTTP`、`RABBITMQ`
- `notifyConfig`：通知配置
- `parameterJson`：发送参数
- `houseNumber`：门牌号

门牌规则：

- 如果方法上是 `houses="1"`，`houseNumber` 为空时会自动补成 `1`
- 如果方法上是 `houses="1"`，传入的 `houseNumber` 只能是 `1`
- 如果方法上是多门牌范围，比如 `houses="0-9"`，业务必须显式传 `houseNumber`
- `houseNumber` 必须落在方法声明的门牌范围内

## 6. 业务接入方式

### 6.1 方式一：消息实体就是方法参数

```java
import com.yw.local.task.message.LocalTaskMessage;
import com.yw.local.task.message.domain.model.entity.LocalTaskMessageEntityCommand;
import com.yw.local.task.message.domain.model.vo.enums.TaskMessageNotifyEnum;

@Service
public class OrderService {

    @LocalTaskMessage(houses = "0-9")
    public void createOrder(LocalTaskMessageEntityCommand command) {
        command.setTaskId("order-create-10001");
        command.setTaskName("orderCreate");
        command.setNotifyType(TaskMessageNotifyEnum.HTTP);
        command.setNotifyConfig(LocalTaskMessageEntityCommand.NotifyConfig.builder()
                .http(LocalTaskMessageEntityCommand.NotifyConfig.HTTP.builder()
                        .url("http://127.0.0.1:8088/callback/order")
                        .method("POST")
                        .build())
                .build());
        command.setParameterJson("{\"orderId\":10001}");
        command.setHouseNumber(3);

        // 业务逻辑
    }
}
```

### 6.2 方式二：消息实体嵌套在 DTO 里

```java
import com.yw.local.task.message.LocalTaskMessage;
import com.yw.local.task.message.domain.model.entity.LocalTaskMessageEntityCommand;

@Service
public class CouponService {

    @LocalTaskMessage(houses = "1", entityAttributeName = "request.messageCommand")
    public void sendCoupon(CouponRequest request) {
        // 业务逻辑
    }

    public static class CouponRequest {
        private LocalTaskMessageEntityCommand messageCommand;

        public LocalTaskMessageEntityCommand getMessageCommand() {
            return messageCommand;
        }

        public void setMessageCommand(LocalTaskMessageEntityCommand messageCommand) {
            this.messageCommand = messageCommand;
        }
    }
}
```

## 7. 通知配置示例

### 7.1 HTTP 通知

```java
LocalTaskMessageEntityCommand.NotifyConfig notifyConfig =
        LocalTaskMessageEntityCommand.NotifyConfig.builder()
                .http(LocalTaskMessageEntityCommand.NotifyConfig.HTTP.builder()
                        .url("http://127.0.0.1:8088/api/callback")
                        .method("POST")
                        .headers(Collections.singletonMap("Authorization", "Bearer demo-token"))
                        .build())
                .build();
```

说明：

- `method` 为空时默认 `POST`
- `GET` 请求不会带请求体
- 非 `GET` 请求会把 `parameterJson` 作为请求体发送

### 7.2 RabbitMQ 通知

```java
LocalTaskMessageEntityCommand.NotifyConfig notifyConfig =
        LocalTaskMessageEntityCommand.NotifyConfig.builder()
                .mq(LocalTaskMessageEntityCommand.NotifyConfig.MQ.builder()
                        .exchange("demo.exchange")
                        .topic("demo.routing.key")
                        .build())
                .build();
```

说明：

- RabbitMQ 会把 `parameterJson` 作为消息体发送
- `topic` 实际作为 `routingKey` 使用

## 8. 补偿规则说明

补偿行为如下：

- 只扫描状态为 `FAILED` 的消息
- 只扫描当前 handler 负责的门牌范围
- 单次最多拉取 `batch-size` 条
- 每条消息异步补偿，互不影响
- 每次 XXL 补偿发送失败，内存计数加 `1`
- 达到 `maxRetry` 后，状态改为 `MANUAL_PROCESSING`

当前方案的重要限制：

- 重试次数只保存在当前应用内存中
- 应用重启后，内存重试次数会丢失
- `maxRetry` 只统计 XXL 补偿失败次数
- 首次即时发送失败不计入 `maxRetry`

## 9. 状态流转

```text
CREATED -> COMPLETED
CREATED -> FAILED -> COMPLETED
CREATED -> FAILED -> MANUAL_PROCESSING
```

含义：

- `CREATED`：消息已落库，等待即时发送或刚进入发送流程
- `COMPLETED`：发送成功
- `FAILED`：发送失败，等待 XXL 补偿
- `MANUAL_PROCESSING`：补偿失败次数达到上限，转人工处理

## 10. 推荐接入步骤

1. 执行建表 SQL
2. 在宿主项目配置 `xxl.job.*`
3. 在宿主项目配置 `local.task.message.*`
4. 在业务方法上增加 `@LocalTaskMessage`
5. 组装 `LocalTaskMessageEntityCommand`
6. 验证即时发送成功链路
7. 人工制造失败，验证 `FAILED -> 补偿 -> COMPLETED`
8. 再验证达到上限后进入 `MANUAL_PROCESSING`
