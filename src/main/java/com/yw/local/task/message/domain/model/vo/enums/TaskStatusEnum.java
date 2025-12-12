package com.yw.local.task.message.domain.model.vo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @Author: yw
 * @Date: 2025/11/30 18:43
 * @Description: 任务状态枚举：0-创建，2-已完成，3-失败
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum TaskStatusEnum {

    CREATED(0, "创建"),
    COMPLETED(2, "已完成"),
    FAILED(3, "失败");

    private Integer code;      // 状态码
    private String desc;       // 状态描述

    // 可以添加一个方法来根据状态码获取枚举对象
    public static TaskStatusEnum fromCode(Integer code) {
        for (TaskStatusEnum status : TaskStatusEnum.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;  // 如果没有匹配的状态码，返回 null
    }
}
