package com.yw.local.task.message.infrastructure.dao;

import com.yw.local.task.message.infrastructure.dao.po.LocalTaskMessagePO;

import java.sql.SQLException;
import java.util.List;

/**
 * 本地消息表 JDBC 访问接口。
 */
public interface LocalTaskMessageMapper {

    /**
     * 插入任务消息。
     */
    void insert(LocalTaskMessagePO record) throws SQLException;

    /**
     * 更新任务状态。
     */
    void updateStatus(String taskId, Integer status);

    /**
     * 查询门牌范围内的失败消息。
     */
    List<LocalTaskMessagePO> queryCompensationMessages(List<Integer> houseNumbers, Integer status, int batchSize);
}
