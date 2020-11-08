package com.accedo.vtc.common;

import android.content.Context;
import android.content.SharedPreferences;


public class SharedPreferencesUtil {
    private static SharedPreferences pref;
    private static SharedPreferencesUtil sharedPreferencesUtil;
    private static SharedPreferencesUtil sShared;

    public static final String TOKEN = "token_firebase";



    public static void initialize(Context context, int mode) {
        if (pref == null) {
            String name = context.getApplicationContext().getPackageName() + "_preferences";
            pref = context.getApplicationContext().getSharedPreferences(name, mode);
        }
    }

    public static SharedPreferencesUtil getInstance() {
        if (sharedPreferencesUtil == null) {
            sharedPreferencesUtil = new SharedPreferencesUtil();
        }
        return sharedPreferencesUtil;
    }

    public static String getTOKEN() {
        return TOKEN;
    }
}
