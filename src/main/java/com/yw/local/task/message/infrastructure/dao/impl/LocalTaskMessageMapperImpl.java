package com.yw.local.task.message.infrastructure.dao.impl;

import com.yw.local.task.message.infrastructure.dao.LocalTaskMessageMapper;
import com.yw.local.task.message.infrastructure.dao.po.LocalTaskMessagePO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * 本地消息表 JDBC 实现。
 */
@Service
@Slf4j
public class LocalTaskMessageMapperImpl implements LocalTaskMessageMapper {

    @Resource
    private DataSource dataSource;

    @Override
    public void insert(LocalTaskMessagePO record) throws SQLException {
        String sql = "INSERT INTO local_task_message (" +
                "task_id, task_name, notify_type, notify_config, status, parameter_json, house_number) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, record.getTaskId());
            ps.setString(2, record.getTaskName());
            ps.setString(3, record.getNotifyType());
            ps.setString(4, record.getNotifyConfig());
            ps.setInt(5, record.getStatus());
            ps.setString(6, record.getParameterJson());
            ps.setInt(7, record.getHouseNumber());

            int rows = ps.executeUpdate();
            if (rows != 1) {
                throw new RuntimeException("插入本地消息失败，taskId=" + record.getTaskId());
            }
        } catch (SQLException e) {
            log.error("插入本地消息失败，taskId={}", record.getTaskId(), e);
            throw e;
        }
    }

    @Override
    public void updateStatus(String taskId, Integer status) {
        String sql = "UPDATE local_task_message SET status = ?, update_time = CURRENT_TIMESTAMP WHERE task_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, status);
            ps.setString(2, taskId);

            int rows = ps.executeUpdate();
            if (rows != 1) {
                throw new RuntimeException("更新本地消息状态失败，taskId=" + taskId + "，status=" + status);
            }
        } catch (SQLException e) {
            log.error("更新本地消息状态失败，taskId={}，status={}", taskId, status, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<LocalTaskMessagePO> queryCompensationMessages(List<Integer> houseNumbers, Integer status, int batchSize) {
        if (houseNumbers == null || houseNumbers.isEmpty()) {
            return new ArrayList<LocalTaskMessagePO>();
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id, task_id, task_name, notify_type, notify_config, status, parameter_json, house_number, create_time, update_time ")
                .append("FROM local_task_message WHERE status = ? AND house_number IN (");
        for (int i = 0; i < houseNumbers.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("?");
        }
        sql.append(") ORDER BY update_time ASC LIMIT ?");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {

            int index = 1;
            ps.setInt(index++, status);
            for (Integer houseNumber : houseNumbers) {
                ps.setInt(index++, houseNumber);
            }
            ps.setInt(index, batchSize);

            try (ResultSet rs = ps.executeQuery()) {
                List<LocalTaskMessagePO> result = new ArrayList<LocalTaskMessagePO>();
                while (rs.next()) {
                    result.add(mapRecord(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            log.error("查询待补偿消息失败，houses={}", houseNumbers, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * ResultSet 到持久化对象的转换收口在这里，避免查询方法里反复铺字段。
     */
    private LocalTaskMessagePO mapRecord(ResultSet rs) throws SQLException {
        LocalTaskMessagePO record = new LocalTaskMessagePO();
        record.setId(rs.getLong("id"));
        record.setTaskId(rs.getString("task_id"));
        record.setTaskName(rs.getString("task_name"));
        record.setNotifyType(rs.getString("notify_type"));
        record.setNotifyConfig(rs.getString("notify_config"));
        record.setStatus(rs.getInt("status"));
        record.setParameterJson(rs.getString("parameter_json"));
        record.setHouseNumber(rs.getInt("house_number"));

        Timestamp createTime = rs.getTimestamp("create_time");
        if (createTime != null) {
            record.setCreateTime(createTime.toLocalDateTime());
        }

        Timestamp updateTime = rs.getTimestamp("update_time");
        if (updateTime != null) {
            record.setUpdateTime(updateTime.toLocalDateTime());
        }
        return record;
    }
}
