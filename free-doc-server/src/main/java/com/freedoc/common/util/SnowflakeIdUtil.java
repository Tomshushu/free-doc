package com.freedoc.common.util;

import cn.hutool.core.util.IdUtil;

public class SnowflakeIdUtil {

    private SnowflakeIdUtil() {
    }

    public static String nextId() {
        return String.valueOf(IdUtil.getSnowflakeNextId());
    }

}
