package com.yw.local.task.message.infrastructure.dao.impl;

import com.yw.local.task.message.infrastructure.dao.LocalTaskMessageMapper;
import com.yw.local.task.message.infrastructure.dao.po.LocalTaskMessagePO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @Author: yw
 * @Date: 2025/11/30 18:40
 * @Description:
 **/
@Service
@Slf4j
public class LocalTaskMessageMapperImpl implements LocalTaskMessageMapper {

    @Resource
    private DataSource dataSource;

    @Override
    public void insert(LocalTaskMessagePO record){
        // SQL 插入语句
        String sql = "INSERT INTO local_task_message (" +
                "task_id, task_name, notify_type, notify_config," +
                "parameter_json, house_number) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, record.getTaskId());
            ps.setString(2, record.getTaskName());
            ps.setString(3, record.getNotifyType());
            ps.setString(4, record.getNotifyConfig());
            ps.setString(5, record.getParameterJson());
            ps.setInt(6, record.getHouseNumber());

            int rows = ps.executeUpdate();

            if (rows != 1) {
                throw new RuntimeException("本地消息组件插入数据失败 未成功插入一个 taskId:{}" + record.getTaskId());
            }
        } catch (SQLException e) {
            log.error("本地消息组件插入数据失败 taskId:{}", record.getTaskId(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateTaskStatusSuccess(LocalTaskMessagePO localTaskMessage) {
        String sql = "UPDATE local_task_message SET status = ? WHERE task_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, localTaskMessage.getStatus());
            ps.setString(2, localTaskMessage.getTaskId());

            int rows = ps.executeUpdate();
            if (rows != 1) {
                throw new RuntimeException("本地消息组件更新数据失败 未成功更新一个 taskId:{}" + localTaskMessage.getTaskId());
            }
        } catch (SQLException e) {
            log.error("本地消息组件更新数据失败 taskId:{}", localTaskMessage.getTaskId(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateTaskStatusFail(LocalTaskMessagePO localTaskMessage) {
        String sql = "UPDATE local_task_message SET status = ? WHERE task_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, localTaskMessage.getStatus());
            ps.setString(2, localTaskMessage.getTaskId());

            int rows = ps.executeUpdate();
            if (rows != 1) {
                throw new RuntimeException("本地消息组件更新数据失败 未成功更新一个 taskId:{}" + localTaskMessage.getTaskId());
            }
        } catch (SQLException e) {
            log.error("本地消息组件更新数据失败 taskId:{}", localTaskMessage.getTaskId(), e);
            throw new RuntimeException(e);
        }
    }
}
