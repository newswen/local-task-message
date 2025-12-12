package com.yw.local.task.message.infrastructure.dao.impl;

import com.yw.local.task.message.infrastructure.dao.LocalTaskMessageMapper;
import com.yw.local.task.message.infrastructure.dao.po.LocalTaskMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
    private JdbcTemplate jdbcTemplate;

    @Override
    public void insert(LocalTaskMessage record) {
        // SQL 插入语句
        String sql = "INSERT INTO local_task_message (" +
                "task_id, task_name, notify_type, notify_config," +
                "parameter_json, house_number) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        int rows = jdbcTemplate.update(sql,
                record.getTaskId(),
                record.getTaskName(),
                record.getNotifyType(),
                record.getNotifyConfig(),
                record.getParameterJson(),
                record.getHouseNumber());

        if (rows != 1) {
            log.error("本地消息组件插入数据失败 record:{}", record);
            throw new RuntimeException("本地消息组件插入数据失败");
        }
    }
}
