package me.weishu.epic.samples.tests.custom;

import android.util.Log;

import com.taobao.android.dexposed.DexposedBridge;
import com.taobao.android.dexposed.XC_MethodHook;

import java.util.Arrays;

/**
 * Created by weishu on 18/1/11.
 */
public class Case18_returnConst implements Case {
    private static final String TAG = "Case18_returnConst";
    @Override
    public void hook() {
        DexposedBridge.findAndHookMethod(Target.class, "returnConst", int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                param.setResult(124);
                Log.d(TAG, "beforeHookedMethod() called with: param = [" + Arrays.toString(param.args) + "]");
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Log.d(TAG, "afterHookedMethod() called with: param = [" + Arrays.toString(param.args) + "]");
            }
        });
    }

    @Override
    public boolean validate(Object... args) {

        int i = new Target().returnConst(123);
        Log.i(TAG, "return : " + i);
        return (i == 124);
    }
}
