package com.yw.local.task.message.infrastructure.dao;

import com.yw.local.task.message.infrastructure.dao.po.LocalTaskMessage;

import java.sql.SQLException;

/**
 * @Author: yw
 * @Date: 2025/11/30 18:40
 * @Description:
 **/
public interface LocalTaskMessageMapper {

    /**
     * 插入任务消息
     */
    void insert(LocalTaskMessage record);

    void updateTaskStatusSuccess(LocalTaskMessage localTaskMessage);

    void updateTaskStatusFail(LocalTaskMessage localTaskMessage);
}
