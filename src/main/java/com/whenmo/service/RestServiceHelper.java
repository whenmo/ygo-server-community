package com.whenmo.service;

import java.io.File;

public class RestServiceHelper {

    public static String getValidTrimString(String str, String errorMessage) {
        if (str == null)
            throw new IllegalArgumentException(errorMessage);
        String trimStr = str.trim();
        if (trimStr.isEmpty())
            throw new IllegalArgumentException(errorMessage);
        return trimStr;
    }

    public static void validateFilePath(String filePath) {
        String trimFilePath = getValidTrimString(filePath, "檔案路徑不可為空");

        File file = new File(trimFilePath);
        if (!file.exists() || !file.isFile())
            throw new IllegalArgumentException("檔案不存在或路徑無效: " + trimFilePath);
    }
}
