package com.yw.local.task.message.domain.service;

import com.yw.local.task.message.domain.model.entity.LocalTaskMessageEntityCommand;
import com.yw.local.task.message.infrastructure.adapter.entity.LocalTaskMessageEvent;

/**
 * 用于后续的MQ&Http等消息监听处理
 *
 * @author: yuanwen
 * @since: 2026/5/10
 */
public interface ILocalTaskMessageNotifyService {

    /**
     * 调用然后回调结果-这里可以新增一个回调结果
     *
     * @return
     */
    String notify(LocalTaskMessageEntityCommand event);

}
