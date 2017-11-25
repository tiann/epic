package com.taobao.android.dexposed.utility;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import java.lang.reflect.Method;

public class NougatPolicy {

    private static class TraceLogger {
        static void i(String tag, String msg) {
            Log.i(tag, msg);
        }
        static void e(String tag, String msg) {
            Log.i(tag, msg);
        }
        static void e(String tag, String msg, Throwable e) {
            Log.i(tag, msg, e);
        }
    }

    private static final String TAG = "NougatPolicy";

    public static boolean fullCompile(Context context) {
        try {
            long t1 = SystemClock.elapsedRealtime();
            Object pm = getPackageManagerBinderProxy();
            if (pm == null) {
                TraceLogger.e(TAG, "can not found package service");
                return false;
            }
            /*
            @Override
            public boolean performDexOptMode(String packageName,
            boolean checkProfiles, String targetCompilerFilter, boolean force) {
                int dexOptStatus = performDexOptTraced(packageName, checkProfiles,
                        targetCompilerFilter, force);
                return dexOptStatus != PackageDexOptimizer.DEX_OPT_FAILED;
            */

            final Method performDexOptMode = pm.getClass().getDeclaredMethod("performDexOptMode",
                    String.class, boolean.class, String.class, boolean.class);
            boolean ret = (boolean) performDexOptMode.invoke(pm, context.getPackageName(), false, "speed", true);
            long cost = SystemClock.elapsedRealtime() - t1;
            Log.i(TAG, "full Compile cost: " + cost + " result:" + ret);
            return ret;
        } catch (Throwable e) {
            TraceLogger.e(TAG, "fullCompile failed:", e);
            return false;
        }
    }

    public static boolean clearCompileData(Context context) {
        boolean ret;
        try {
            Object pm = getPackageManagerBinderProxy();
            final Method performDexOpt = pm.getClass().getDeclaredMethod("performDexOpt", String.class,
                    boolean.class, int.class, boolean.class);
            ret = (Boolean) performDexOpt.invoke(pm, context.getPackageName(), false, 2 /*install*/, true);
        } catch (Throwable e) {
            TraceLogger.e(TAG, "clear compile data failed", e);
            ret = false;
        }
        return ret;
    }

    private static Object getPackageManagerBinderProxy() throws Exception {
        Class<?> activityThread = Class.forName("android.app.ActivityThread");
        final Method getPackageManager = activityThread.getDeclaredMethod("getPackageManager");
        return getPackageManager.invoke(null);
    }
}
