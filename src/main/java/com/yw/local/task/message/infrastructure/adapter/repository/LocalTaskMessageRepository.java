package com.yw.local.task.message.infrastructure.adapter.repository;

import com.alibaba.fastjson.JSON;
import com.yw.local.task.message.config.properties.LocalTaskMessageProperties;
import com.yw.local.task.message.domain.adapter.repository.ILocalTaskMessageRepository;
import com.yw.local.task.message.domain.model.entity.LocalTaskMessageEntityCommand;
import com.yw.local.task.message.domain.model.vo.enums.TaskMessageNotifyEnum;
import com.yw.local.task.message.domain.model.vo.enums.TaskStatusEnum;
import com.yw.local.task.message.infrastructure.dao.LocalTaskMessageMapper;
import com.yw.local.task.message.infrastructure.dao.po.LocalTaskMessagePO;
import com.yw.local.task.message.infrastructure.support.LocalTaskMessageHouseContext;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 本地消息仓储实现。
 * <p>
 * 负责消息实体与数据库记录之间的转换，并在落库前做门牌校验。
 */
@Repository
public class LocalTaskMessageRepository implements ILocalTaskMessageRepository {

    @Resource
    private LocalTaskMessageMapper localTaskMessageMapper;

    @Resource
    private LocalTaskMessageProperties localTaskMessageProperties;

    @Override
    public void saveTaskMessage(LocalTaskMessageEntityCommand command) throws SQLException {
        Integer houseNumber = resolveHouseNumber(command);
        command.setHouseNumber(houseNumber);
        command.setStatus(TaskStatusEnum.CREATED);

        LocalTaskMessagePO localTaskMessagePO = new LocalTaskMessagePO();
        localTaskMessagePO.setTaskId(command.getTaskId());
        localTaskMessagePO.setTaskName(command.getTaskName());
        localTaskMessagePO.setNotifyType(command.getNotifyType().getCode());
        localTaskMessagePO.setNotifyConfig(JSON.toJSONString(command.getNotifyConfig()));
        localTaskMessagePO.setParameterJson(command.getParameterJson());
        localTaskMessagePO.setHouseNumber(houseNumber);
        localTaskMessagePO.setStatus(TaskStatusEnum.CREATED.getCode());

        localTaskMessageMapper.insert(localTaskMessagePO);
    }

    @Override
    public void updateTaskStatusSuccess(LocalTaskMessageEntityCommand event) {
        localTaskMessageMapper.updateStatus(event.getTaskId(), TaskStatusEnum.COMPLETED.getCode());
    }

    @Override
    public void updateTaskStatusFail(LocalTaskMessageEntityCommand event) {
        localTaskMessageMapper.updateStatus(event.getTaskId(), TaskStatusEnum.FAILED.getCode());
    }

    @Override
    public void updateTaskStatusManualProcessing(LocalTaskMessageEntityCommand event) {
        localTaskMessageMapper.updateStatus(event.getTaskId(), TaskStatusEnum.MANUAL_PROCESSING.getCode());
    }

    @Override
    public List<LocalTaskMessageEntityCommand> queryCompensationMessages(List<Integer> houseNumbers, int batchSize) {
        List<LocalTaskMessagePO> records = localTaskMessageMapper.queryCompensationMessages(
                houseNumbers,
                TaskStatusEnum.FAILED.getCode(),
                batchSize
        );

        List<LocalTaskMessageEntityCommand> commands = new ArrayList<LocalTaskMessageEntityCommand>(records.size());
        for (LocalTaskMessagePO record : records) {
            LocalTaskMessageEntityCommand command = new LocalTaskMessageEntityCommand();
            command.setTaskId(record.getTaskId());
            command.setTaskName(record.getTaskName());
            command.setNotifyType(TaskMessageNotifyEnum.fromCode(record.getNotifyType()));
            command.setNotifyConfig(JSON.parseObject(record.getNotifyConfig(), LocalTaskMessageEntityCommand.NotifyConfig.class));
            command.setStatus(TaskStatusEnum.fromCode(record.getStatus()));
            command.setParameterJson(record.getParameterJson());
            command.setHouseNumber(record.getHouseNumber());
            commands.add(command);
        }
        return commands;
    }

    /**
     * 门牌规则统一在仓储层收口。
     * <p>
     * 无论业务方从哪里构建 command，最终只要进到落库流程，
     * 都必须遵循方法注解声明的门牌范围。
     */
    private Integer resolveHouseNumber(LocalTaskMessageEntityCommand command) {
        validateCommand(command);

        LocalTaskMessageHouseContext.RouteContext routeContext = LocalTaskMessageHouseContext.getRequired();
        List<Integer> houseNumbers = routeContext.getHouseNumbers();
        if (houseNumbers == null || houseNumbers.isEmpty()) {
            throw new IllegalStateException("当前方法未声明可用门牌范围，方法：" + routeContext.getMethodSignature());
        }

        Integer explicitHouseNumber = command.getHouseNumber();
        if (houseNumbers.size() == 1) {
            return resolveSingleHouseNumber(explicitHouseNumber, houseNumbers.get(0));
        }

        if (explicitHouseNumber == null) {
            throw new IllegalArgumentException("当前业务方法配置了多个门牌范围，必须由业务显式指定 houseNumber，taskName=" + command.getTaskName());
        }
        validateGlobalHouseNumber(explicitHouseNumber);
        if (!houseNumbers.contains(explicitHouseNumber)) {
            throw new IllegalArgumentException("当前业务方法允许的门牌范围为 " + routeContext.getHousesExpression() + "，实际传入：" + explicitHouseNumber);
        }
        return explicitHouseNumber;
    }

    private Integer resolveSingleHouseNumber(Integer explicitHouseNumber, Integer uniqueHouseNumber) {
        if (explicitHouseNumber == null) {
            return uniqueHouseNumber;
        }

        validateGlobalHouseNumber(explicitHouseNumber);
        if (!uniqueHouseNumber.equals(explicitHouseNumber)) {
            throw new IllegalArgumentException("当前业务方法只允许使用门牌 " + uniqueHouseNumber + "，实际传入：" + explicitHouseNumber);
        }
        return explicitHouseNumber;
    }

    private void validateCommand(LocalTaskMessageEntityCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("本地消息实体不能为空");
        }
        if (command.getTaskId() == null || command.getTaskId().trim().isEmpty()) {
            throw new IllegalArgumentException("taskId 不能为空");
        }
        if (command.getTaskName() == null || command.getTaskName().trim().isEmpty()) {
            throw new IllegalArgumentException("taskName 不能为空");
        }
        if (command.getNotifyType() == null) {
            throw new IllegalArgumentException("notifyType 不能为空");
        }
    }

    private void validateGlobalHouseNumber(Integer houseNumber) {
        int totalCount = localTaskMessageProperties.getHouse().getTotalCount();
        if (totalCount <= 0) {
            throw new IllegalArgumentException("local.task.message.house.total-count 必须大于 0");
        }
        if (houseNumber < 0 || houseNumber >= totalCount) {
            throw new IllegalArgumentException("门牌号超出全局范围，houseNumber=" + houseNumber + "，totalCount=" + totalCount);
        }
    }
}
