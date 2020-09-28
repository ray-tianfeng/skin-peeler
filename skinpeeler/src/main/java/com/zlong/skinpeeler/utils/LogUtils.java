package com.zlong.skinpeeler.utils;

import android.util.Log;

/**
 * Time: 2020/9/24 0024
 * Author: zoulong
 */
public class LogUtils {
    private final static String TAG = "skin_peeler";
    public static void d(String msg){
        Log.d(TAG, msg);
    }

    public static void e(String msg) {
        Log.e(TAG, msg);
    }
}
