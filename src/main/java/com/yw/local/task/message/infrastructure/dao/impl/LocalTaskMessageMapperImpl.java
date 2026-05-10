package com.yw.local.task.message.infrastructure.dao.impl;

import com.yw.local.task.message.infrastructure.dao.LocalTaskMessageMapper;
import com.yw.local.task.message.infrastructure.dao.po.LocalTaskMessage;
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
    public void insert(LocalTaskMessage record) throws SQLException {
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
        } catch (Exception e) {
            log.error("本地消息组件插入数据失败 taskId:{}", record.getTaskId(), e);
            throw e;
        }
    }
}
