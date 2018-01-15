package me.weishu.epic.samples.tests.custom;

import android.util.Log;

import java.util.Arrays;

import de.robv.android.xposed.DexposedBridge;
import de.robv.android.xposed.XC_MethodHook;

/**
 * Created by weishu on 17/11/8.
 */

public class Case7 implements Case {

    private static final String TAG = "Case7";

    @Override
    public void hook() {
        Log.i(TAG, "hook test1");
        DexposedBridge.findAndHookMethod(Target.class, "test1", Object.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.i(TAG, "before add hooked:" + Arrays.toString(param.args));
                param.setResult(4);
                super.beforeHookedMethod(param);
            }
        });

    }

    @Override
    public boolean validate(Object... args) {
        Target t = new Target();
        t.test1(t, 123);
        return true;
    }
}
