package com.yw.local.task.message.infrastructure.adapter.entity;

import com.yw.local.task.message.domain.model.entity.LocalTaskMessageEntityCommand;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @Author: yw
 * @Date: 2025/11/30 15:53
 * @Description:Spring Event发送消息事件 该事件包含到消息本体
 **/
@Getter
public class LocalTaskMessageEvent extends ApplicationEvent {

    private LocalTaskMessageEntityCommand localTaskMessageEntityCommand;


    public LocalTaskMessageEvent(Object source, LocalTaskMessageEntityCommand localTaskMessageEntityCommand) {
        super(source);
        this.localTaskMessageEntityCommand = localTaskMessageEntityCommand;
    }
}
