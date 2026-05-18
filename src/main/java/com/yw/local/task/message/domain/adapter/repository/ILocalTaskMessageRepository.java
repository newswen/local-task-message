package com.yw.local.task.message.domain.adapter.repository;

import com.yw.local.task.message.domain.model.entity.LocalTaskMessageEntityCommand;

import java.sql.SQLException;
import java.util.List;

/**
 * 本地消息仓储接口。
 */
public interface ILocalTaskMessageRepository {

    /**
     * 保存任务消息。
     *
     * @param localTaskMessageEntityCommand 本地消息实体
     */
    void saveTaskMessage(LocalTaskMessageEntityCommand localTaskMessageEntityCommand) throws SQLException;

    /**
     * 把消息状态更新为完成。
     */
    void updateTaskStatusSuccess(LocalTaskMessageEntityCommand event);

    /**
     * 把消息状态更新为失败。
     */
    void updateTaskStatusFail(LocalTaskMessageEntityCommand event);

    /**
     * 把消息状态更新为人工处理。
     */
    void updateTaskStatusManualProcessing(LocalTaskMessageEntityCommand event);

    /**
     * 查询某一批门牌下待补偿的失败消息。
     */
    List<LocalTaskMessageEntityCommand> queryCompensationMessages(List<Integer> houseNumbers, int batchSize);
}
