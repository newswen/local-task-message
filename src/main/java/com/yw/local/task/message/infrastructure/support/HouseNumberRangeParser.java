package com.yw.local.task.message.infrastructure.support;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 门牌范围解析工具。
 * <p>
 * 注解和 XXL handler 使用同一套解析规则，
 * 避免业务方法与补偿任务对门牌表达式的理解不一致。
 */
public final class HouseNumberRangeParser {

    private HouseNumberRangeParser() {
    }

    /**
     * 解析门牌表达式。
     *
     * @param houses 门牌表达式
     * @param totalCount 全局门牌总数
     * @return 去重后的门牌集合，保持原始声明顺序
     */
    public static List<Integer> parse(String houses, int totalCount) {
        if (houses == null || houses.trim().isEmpty()) {
            throw new IllegalArgumentException("门牌范围配置不能为空");
        }
        if (totalCount <= 0) {
            throw new IllegalArgumentException("门牌总数必须大于 0");
        }

        Set<Integer> result = new LinkedHashSet<Integer>();
        String[] segments = houses.split(",");
        for (String segment : segments) {
            String value = segment.trim();
            if (value.isEmpty()) {
                continue;
            }

            try {
                if (value.contains("-")) {
                    appendRange(value, totalCount, result);
                } else {
                    int houseNumber = Integer.parseInt(value);
                    validateHouseNumber(houseNumber, totalCount);
                    result.add(houseNumber);
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("非法门牌配置：" + value, e);
            }
        }

        if (result.isEmpty()) {
            throw new IllegalArgumentException("门牌范围配置不能为空");
        }
        return new ArrayList<Integer>(result);
    }

    private static void appendRange(String value, int totalCount, Set<Integer> result) {
        String[] range = value.split("-");
        if (range.length != 2) {
            throw new IllegalArgumentException("非法门牌范围：" + value);
        }

        int start = Integer.parseInt(range[0].trim());
        int end = Integer.parseInt(range[1].trim());
        if (start > end) {
            throw new IllegalArgumentException("非法门牌范围：" + value);
        }

        for (int i = start; i <= end; i++) {
            validateHouseNumber(i, totalCount);
            result.add(i);
        }
    }

    private static void validateHouseNumber(int houseNumber, int totalCount) {
        if (houseNumber < 0 || houseNumber >= totalCount) {
            throw new IllegalArgumentException("门牌号超出范围，houseNumber=" + houseNumber + "，totalCount=" + totalCount);
        }
    }
}
