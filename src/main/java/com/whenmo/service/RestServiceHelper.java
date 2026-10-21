package com.whenmo.service;

import java.io.File;

public class RestServiceHelper {

    public static void validateString(String param, String paramName) {
        if (param == null || param.isBlank())
            throw new IllegalArgumentException(paramName + " 不可為空");
    }

    public static void validateFilePath(String filePath) {
        validateString(filePath, "檔案路徑");

        File file = new File(filePath);
        if (!file.exists() || !file.isFile())
            throw new IllegalArgumentException("檔案不存在或路徑無效: " + filePath);
    }
}
