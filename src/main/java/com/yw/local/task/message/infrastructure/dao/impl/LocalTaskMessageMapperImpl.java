package com.yw.local.task.message.infrastructure.dao.impl;

import com.yw.local.task.message.infrastructure.dao.LocalTaskMessageMapper;
import com.yw.local.task.message.infrastructure.dao.po.LocalTaskMessage;
import org.springframework.stereotype.Service;

/**
 * @Author: yw
 * @Date: 2025/11/30 18:40
 * @Description:
 **/
@Service
public class LocalTaskMessageMapperImpl implements LocalTaskMessageMapper {

    @Override
    public int insert(LocalTaskMessage record) {
        //todo 插入任务消息
        return 0;
    }
}
