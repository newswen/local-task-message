package com.yw.local.task.message.domain.service;

import com.yw.local.task.message.domain.model.entity.LocalTaskMessageEntityCommand;

/**
 * @Author: yw
 * @Date: 2025/11/30 16:15
 * @Description: 提供给外部调用的本地消息组件服务接口
 **/
public interface ILocalTaskMessageHandleService {

    void handleLocalTaskMessage(LocalTaskMessageEntityCommand command);

}
