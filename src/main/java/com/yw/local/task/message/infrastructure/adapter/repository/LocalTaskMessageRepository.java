package com.yw.local.task.message.infrastructure.adapter.repository;

import com.alibaba.fastjson.JSON;
import com.yw.local.task.message.domain.adapter.repository.ILocalTaskMessageRepository;
import com.yw.local.task.message.domain.model.entity.LocalTaskMessageEntityCommand;
import com.yw.local.task.message.infrastructure.dao.LocalTaskMessageMapper;
import com.yw.local.task.message.infrastructure.dao.po.LocalTaskMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.sql.SQLException;

/**
 * @Author: yw
 * @Date: 2025/11/30 18:39
 * @Description:
 **/
@Repository
@Slf4j
public class LocalTaskMessageRepository implements ILocalTaskMessageRepository {

    @Resource
    private LocalTaskMessageMapper localTaskMessageMapper;

    @Override
    public void saveTaskMessage(LocalTaskMessageEntityCommand localTaskMessageEntityCommand) throws SQLException {

        LocalTaskMessage localTaskMessage = new LocalTaskMessage();
        localTaskMessage.setTaskId(localTaskMessageEntityCommand.getTaskId());
        localTaskMessage.setTaskName(localTaskMessageEntityCommand.getTaskName());
        localTaskMessage.setNotifyType(localTaskMessageEntityCommand.getNotifyType().getCode());
        localTaskMessage.setNotifyConfig(JSON.toJSONString(localTaskMessageEntityCommand.getNotifyConfig()));
        localTaskMessage.setParameterJson(JSON.toJSONString(localTaskMessageEntityCommand.getParameterJson()));

        //门牌号 设置为0-9 10个定时任务
        int hashCode = Math.abs(localTaskMessageEntityCommand.getTaskId().hashCode());
        int houseNumber = hashCode % 10;
        localTaskMessage.setHouseNumber(houseNumber);

        localTaskMessageMapper.insert(localTaskMessage);
    }
}
