package com.yw.local.task.message.infrastructure.adapter.repository;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.yw.local.task.message.domain.adapter.repository.ILocalTaskMessageRepository;
import com.yw.local.task.message.domain.model.entity.LocalTaskMessageEntityCommand;
import com.yw.local.task.message.infrastructure.dao.LocalTaskMessageMapper;
import com.yw.local.task.message.infrastructure.dao.po.LocalTaskMessage;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.sql.SQLException;

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
    public void saveTaskMessage(LocalTaskMessageEntityCommand dto) throws SQLException {
        LocalTaskMessage localTaskMessage = LocalTaskMessage.builder()
                .taskId(dto.getTaskId())
                .taskName(dto.getTaskName())
                .notifyType(dto.getNotifyType().getCode())
                //对于null的数据不加到json中
                .notifyConfig(JSON.toJSONString(dto.getNotifyConfig()))
                .parameterJson(dto.getParameterJson())
                .build();
        //定时任务扫描设置门牌号 根据任务ID分配到0-9 哈希
        localTaskMessage.setHouseNumber(Math.abs(dto.getTaskId().hashCode() % 10));
        localTaskMessageMapper.insert(localTaskMessage);
    }
}
