package com.yw.local.task.message.domain.adapter.repository;

import com.yw.local.task.message.domain.model.entity.LocalTaskMessageEntityCommand;

import java.sql.SQLException;

/**
 * @Author: yw
 * @Date: 2025/11/30 18:38
 * @Description: 本地消息组件数据仓库接口
 **/
public interface ILocalTaskMessageRepository {

    /**
     * 保存任务消息
     */
    void saveTaskMessage(LocalTaskMessageEntityCommand localTaskMessageEntityCommand) throws SQLException;

    void updateTaskStatusSuccess(LocalTaskMessageEntityCommand event);

    void updateTaskStatusFail(LocalTaskMessageEntityCommand event);
}
