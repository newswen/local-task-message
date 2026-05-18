package com.yw.local.task.message.infrastructure.support;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class HouseNumberRangeParserTest {

    @Test
    void shouldParseMixedHouseNumbers() {
        Assertions.assertEquals(Arrays.asList(0, 1, 2, 4, 6, 7), HouseNumberRangeParser.parse("0-2,4,6-7", 10));
    }

    @Test
    void shouldRejectOutOfRangeHouseNumber() {
        Assertions.assertThrows(IllegalArgumentException.class, new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() {
                HouseNumberRangeParser.parse("10", 10);
            }
        });
    }
}
