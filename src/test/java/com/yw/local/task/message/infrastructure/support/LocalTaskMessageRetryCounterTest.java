package com.yw.local.task.message.infrastructure.support;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LocalTaskMessageRetryCounterTest {

    @Test
    void shouldIncrementAndClearRetryCounter() {
        LocalTaskMessageRetryCounter retryCounter = new LocalTaskMessageRetryCounter();
        Assertions.assertEquals(1, retryCounter.increment("task-1"));
        Assertions.assertEquals(2, retryCounter.increment("task-1"));
        Assertions.assertEquals(2, retryCounter.get("task-1"));
        retryCounter.clear("task-1");
        Assertions.assertEquals(0, retryCounter.get("task-1"));
    }
}
