package com.yw.local.task.message.infrastructure.adapter.repository;

import com.yw.local.task.message.config.properties.LocalTaskMessageProperties;
import com.yw.local.task.message.domain.model.entity.LocalTaskMessageEntityCommand;
import com.yw.local.task.message.domain.model.vo.enums.TaskMessageNotifyEnum;
import com.yw.local.task.message.domain.model.vo.enums.TaskStatusEnum;
import com.yw.local.task.message.infrastructure.dao.LocalTaskMessageMapper;
import com.yw.local.task.message.infrastructure.dao.po.LocalTaskMessagePO;
import com.yw.local.task.message.infrastructure.support.LocalTaskMessageHouseContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.SQLException;
import java.util.Arrays;

class LocalTaskMessageRepositoryTest {

    @AfterEach
    void tearDown() {
        LocalTaskMessageHouseContext.clear();
    }

    @Test
    void shouldAutoAssignSingleHouseNumber() throws SQLException {
        LocalTaskMessageMapper mapper = Mockito.mock(LocalTaskMessageMapper.class);
        LocalTaskMessageRepository repository = new LocalTaskMessageRepository();
        ReflectionTestUtils.setField(repository, "localTaskMessageMapper", mapper);
        ReflectionTestUtils.setField(repository, "localTaskMessageProperties", createProperties());

        LocalTaskMessageHouseContext.bind(new LocalTaskMessageHouseContext.RouteContext("demo()", "1", Arrays.asList(1)));
        LocalTaskMessageEntityCommand command = createCommand();
        command.setHouseNumber(null);

        repository.saveTaskMessage(command);

        ArgumentCaptor<LocalTaskMessagePO> captor = ArgumentCaptor.forClass(LocalTaskMessagePO.class);
        Mockito.verify(mapper).insert(captor.capture());
        Assertions.assertEquals(Integer.valueOf(1), captor.getValue().getHouseNumber());
        Assertions.assertEquals(TaskStatusEnum.CREATED, command.getStatus());
    }

    @Test
    void shouldRequireExplicitHouseNumberWhenRangeContainsMultipleValues() {
        Assertions.assertThrows(IllegalArgumentException.class, new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() throws Throwable {
                LocalTaskMessageMapper mapper = Mockito.mock(LocalTaskMessageMapper.class);
                LocalTaskMessageRepository repository = new LocalTaskMessageRepository();
                ReflectionTestUtils.setField(repository, "localTaskMessageMapper", mapper);
                ReflectionTestUtils.setField(repository, "localTaskMessageProperties", createProperties());

                LocalTaskMessageHouseContext.bind(new LocalTaskMessageHouseContext.RouteContext("demo()", "0-2", Arrays.asList(0, 1, 2)));
                LocalTaskMessageEntityCommand command = createCommand();
                command.setHouseNumber(null);

                repository.saveTaskMessage(command);
            }
        });
    }

    @Test
    void shouldAcceptExplicitHouseNumberInRange() throws SQLException {
        LocalTaskMessageMapper mapper = Mockito.mock(LocalTaskMessageMapper.class);
        LocalTaskMessageRepository repository = new LocalTaskMessageRepository();
        ReflectionTestUtils.setField(repository, "localTaskMessageMapper", mapper);
        ReflectionTestUtils.setField(repository, "localTaskMessageProperties", createProperties());

        LocalTaskMessageHouseContext.bind(new LocalTaskMessageHouseContext.RouteContext("demo()", "0-2", Arrays.asList(0, 1, 2)));
        LocalTaskMessageEntityCommand command = createCommand();
        command.setHouseNumber(2);

        repository.saveTaskMessage(command);

        ArgumentCaptor<LocalTaskMessagePO> captor = ArgumentCaptor.forClass(LocalTaskMessagePO.class);
        Mockito.verify(mapper).insert(captor.capture());
        Assertions.assertEquals(Integer.valueOf(2), captor.getValue().getHouseNumber());
    }

    private LocalTaskMessageProperties createProperties() {
        LocalTaskMessageProperties properties = new LocalTaskMessageProperties();
        properties.getHouse().setTotalCount(10);
        return properties;
    }

    private LocalTaskMessageEntityCommand createCommand() {
        LocalTaskMessageEntityCommand command = new LocalTaskMessageEntityCommand();
        command.setTaskId("task-1");
        command.setTaskName("order");
        command.setNotifyType(TaskMessageNotifyEnum.HTTP);
        command.setNotifyConfig(new LocalTaskMessageEntityCommand.NotifyConfig());
        command.setParameterJson("{\"id\":1}");
        return command;
    }
}
