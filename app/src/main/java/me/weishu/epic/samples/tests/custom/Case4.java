package me.weishu.epic.samples.tests.custom;

import android.text.TextUtils;
import android.util.Log;

import de.robv.android.xposed.DexposedBridge;
import de.robv.android.xposed.XC_MethodHook;

/**
 * Created by weishu on 17/11/6.
 */

public class Case4 implements Case {
    private static final String TAG = "Case4";

    @Override
    public void hook() {
        DexposedBridge.findAndHookMethod(TextUtils.class, "equals", CharSequence.class, CharSequence.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.i(TAG, "beforeHookedMethod: this:" + param.thisObject, new RuntimeException("stack"));
                Log.i(TAG, "beforeHookedMethod: String1:" + param.args[0]);
                Log.i(TAG, "beforeHookedMethod: String2:" + param.args[1]);

                param.setResult(false);

                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
    }

    @Override
    public boolean validate(Object... args) {
        String a1 = new String("123");
        String a2 = new String("123");

        Log.i(TAG, " '123' equals '123' ? " + TextUtils.equals(a1, a2));
        return true;
    }
}
