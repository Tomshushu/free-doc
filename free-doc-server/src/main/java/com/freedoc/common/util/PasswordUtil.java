package com.freedoc.common.util;

import cn.hutool.crypto.digest.DigestUtil;

public class PasswordUtil {

    private PasswordUtil() {
    }

    public static String encrypt(String password, String salt) {
        return DigestUtil.md5Hex(salt + password);
    }

    public static boolean verify(String rawPassword, String salt, String encryptedPassword) {
        return encrypt(rawPassword, salt).equals(encryptedPassword);
    }

}
