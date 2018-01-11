package me.weishu.epic.samples.tests.custom;

import android.util.Log;

import com.taobao.android.dexposed.DexposedBridge;
import com.taobao.android.dexposed.XC_MethodHook;

/**
 * Created by weishu on 18/1/11.
 */

public class Case17_SameMethod implements Case {
    private static final String TAG = "Case17_SameMethod";

    @Override
    public void hook() {
        DexposedBridge.findAndHookMethod(Target.class, "add", int.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Log.d(TAG, "beforeHookedMethod() called with: param = [" + param + "]");
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Log.d(TAG, "afterHookedMethod() called with: param = [" + param + "]");
            }
        });

        DexposedBridge.findAndHookMethod(Target.class, "add", int.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Log.d(TAG, "beforeHookedMethod2() called with: param = [" + param + "]");
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Log.d(TAG, "afterHookedMethod2() called with: param = [" + param + "]");
            }
        });
    }

    @Override
    public boolean validate(Object... args) {
        new Target().add(1, 2);
        new Target().add(3, 4);
        return false;
    }
}
