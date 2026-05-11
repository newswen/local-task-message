package com.yw.local.task.message.domain.service.notify.strategy;

import com.yw.local.task.message.domain.model.entity.LocalTaskMessageEntityCommand;
import com.yw.local.task.message.infrastructure.adapter.entity.LocalTaskMessageEvent;

/**
 * 说明
 *
 * @author: yuanwen
 * @since: 2026/5/10
 */
public interface INotifyStartegy {

    String notify(LocalTaskMessageEntityCommand event);

}
