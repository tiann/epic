package me.weishu.epic.samples.tests.custom;

import android.util.Log;

import de.robv.android.xposed.DexposedBridge;
import de.robv.android.xposed.XC_MethodHook;

/**
 * Created by weishu on 17/11/6.
 */

public class Case3 implements Case {
    private static final String TAG = "Case3";

    @Override
    public void hook() {
        DexposedBridge.findAndHookMethod(System.class, "currentTimeMillis", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Log.i("mylog", "before currentTimeMillis");
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Log.i("mylog", "after currentTimeMillis");
            }
        });
    }

    @Override
    public boolean validate(Object... args) {
        Log.i(TAG, "currentTimeMillis: " + System.currentTimeMillis());
        return true;
    }
}
