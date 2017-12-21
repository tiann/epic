package me.weishu.epic.samples.tests.custom;

import android.util.Log;

import com.taobao.android.dexposed.DexposedBridge;
import com.taobao.android.dexposed.XC_MethodHook;

import java.util.Arrays;

import me.weishu.epic.samples.tests.LogMethodHook;

/**
 * Created by weishu on 17/12/19.
 */

public class Case16_Float implements Case {
    @Override
    public void hook() {
        DexposedBridge.findAndHookMethod(Target.class, "testFloat", String.class, long.class, float.class, Object.class, new LogMethodHook());
        DexposedBridge.hookAllConstructors(Target.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Log.i("mylog", "hooked obj1:" + Arrays.toString(param.args));
                param.args[0] = "modified";
            }
        });
    }

    @Override
    public boolean validate(Object... args) {
        // new Target().testFloat("string", 123L, 0.023f, "object");
        Target t = new Target("123");
        Log.i("mylog", "t :" + t + ", " + t.name);
        return false;
    }
}
