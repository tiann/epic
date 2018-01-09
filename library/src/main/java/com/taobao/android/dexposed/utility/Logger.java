package com.taobao.android.dexposed.utility;

import android.util.Log;

/**
 * Created by weishu on 17/11/10.
 */
public class Logger {

    private static final boolean DEBUG = Debug.DEBUG;

    public static final String preFix = "epic.";

    public static void i(String tag, String msg) {
        if (DEBUG) {
            Log.i(preFix + tag, msg);
        }
    }

    public static void d(String tagSuffix, String msg) {
        if (DEBUG) {
            Log.d(preFix + tagSuffix, msg);
        }
    }

    public static void w(String tag, String msg) {
        Log.w(preFix + tag, msg);
    }

    public static void e(String tag, String msg) {
        if (DEBUG) {
            Log.e(preFix + tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable e) {
        if (DEBUG) {
            Log.e(preFix + tag, msg, e);
        }
    }

}
