package com.huazhu.springbootflowable.util;

public class ObjectUtils {

    public static String toString(Object object) {
        if (object == null) {
            return "";
        }
        return org.springframework.util.ObjectUtils.nullSafeToString(object);
    }
}
