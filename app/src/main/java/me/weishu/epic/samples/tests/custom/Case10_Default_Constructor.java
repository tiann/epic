package me.weishu.epic.samples.tests.custom;

import android.util.Log;

import java.lang.reflect.Constructor;

import de.robv.android.xposed.DexposedBridge;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

/**
 * Created by weishu on 17/11/13.
 */

public class Case10_Default_Constructor implements Case {

    private static final String TAG = "Case10_Default_Constructor";

    @Override
    public void hook() {
        Constructor<?> constructor = XposedHelpers.findConstructorExact(Target.class);
        DexposedBridge.hookMethod(constructor, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Log.i(TAG, "Target constructor called");
            }
        });
    }

    @Override
    public boolean validate(Object... args) {
        new Target();
        return true;
    }
}
