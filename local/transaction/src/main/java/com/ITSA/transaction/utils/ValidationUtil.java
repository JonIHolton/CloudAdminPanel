package com.ITSA.transaction.utils;
public class ValidationUtil {

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}