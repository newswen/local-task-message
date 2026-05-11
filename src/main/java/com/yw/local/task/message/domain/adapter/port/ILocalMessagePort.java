package com.yw.local.task.message.domain.adapter.port;

import com.yw.local.task.message.domain.model.entity.LocalTaskMessageEntityCommand;

/**
 * 说明
 *
 * @author: yuanwen
 * @since: 2026/5/10
 */
public interface ILocalMessagePort {

    String notify(LocalTaskMessageEntityCommand event);

}
