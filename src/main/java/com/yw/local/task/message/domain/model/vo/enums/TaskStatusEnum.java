package com.yw.local.task.message.domain.model.vo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务状态枚举。
 */
@Getter
@AllArgsConstructor
public enum TaskStatusEnum {

    /**
     * 已创建，等待即时发送或刚进入发送流程。
     */
    CREATED(0, "创建"),

    /**
     * 已发送成功。
     */
    COMPLETED(1, "完成"),

    /**
     * 发送失败，等待补偿。
     */
    FAILED(2, "失败"),

    /**
     * 补偿达到上限，进入人工处理。
     */
    MANUAL_PROCESSING(3, "人工处理");

    private final Integer code;
    private final String desc;

    public static TaskStatusEnum fromCode(Integer code) {
        for (TaskStatusEnum status : TaskStatusEnum.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知任务状态编码：" + code);
    }
}
