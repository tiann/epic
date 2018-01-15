package me.weishu.epic.samples.tests.custom;

import android.util.Log;

import java.lang.reflect.Method;

import de.robv.android.xposed.XposedHelpers;

/**
 * Created by weishu on 17/12/13.
 */

public class Case13_FastNative implements Case {

    @Override
    public void hook() {
//        DexposedBridge.findAndHookMethod(Target.class, "longRunMethod", new LogMethodHook());
        final Method invoke = XposedHelpers.findMethodExact(Method.class, "invoke", Object.class, Object[].class);
        Log.i("mylog", "invole: " + invoke);
    }

    @Override
    public boolean validate(Object... args) {
        new Target().longRunMethod();
        return true;
    }

}
