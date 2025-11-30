package com.yw.local.task.message.infrastructure.adapter.repository;

import com.yw.local.task.message.domain.adapter.repository.ILocalTaskMessageRepository;
import com.yw.local.task.message.domain.model.entity.LocalTaskMessageEntityCommand;
import com.yw.local.task.message.infrastructure.dao.LocalTaskMessageMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * @Author: yw
 * @Date: 2025/11/30 18:39
 * @Description:
 **/
@Repository
public class LocalTaskMessageRepository implements ILocalTaskMessageRepository {

    @Resource
    private LocalTaskMessageMapper localTaskMessageMapper;

    @Override
    public void saveTaskMessage(LocalTaskMessageEntityCommand localTaskMessageEntityCommand) {
        //todo 保存任务消息
    }
}
