package me.weishu.epic.samples.tests.custom;

import android.util.Log;

import java.util.Arrays;

import de.robv.android.xposed.DexposedBridge;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;

/**
 * Created by weishu on 18/1/11.
 */
public class Case18_returnConst implements Case {
    private static final String TAG = "Case18_returnConst";
    @Override
    public void hook() {
//        DexposedBridge.findAndHookMethod(Target.class, "returnConst1", new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                super.beforeHookedMethod(param);
//                param.setResult(124);
//                Log.d(TAG, "beforeHookedMethod() called with: param = [" + Arrays.toString(param.args) + "]");
//            }
//
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                super.afterHookedMethod(param);
//                Log.d(TAG, "afterHookedMethod() called with: param = [" + Arrays.toString(param.args) + "]");
//            }
//        });

        DexposedBridge.findAndHookMethod(Target.class, "returnConst1", new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                return 124;
            }
        });

//        DexposedBridge.findAndHookMethod(Target.class, "returnConst1", new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                super.beforeHookedMethod(param);
//                param.setResult(12);
//                Log.d(TAG, "beforeHookedMethod11() called with: param = [" + Arrays.toString(param.args) + "]");
//            }
//
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                super.afterHookedMethod(param);
//                Log.d(TAG, "afterHookedMethod11() called with: param = [" + Arrays.toString(param.args) + "]");
//            }
//        });

        DexposedBridge.findAndHookMethod(Target.class, "returnConst2", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                param.setResult(125);
                Log.d(TAG, "beforeHookedMethod2() called with: param = [" + Arrays.toString(param.args) + "]");
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Log.d(TAG, "afterHookedMethod2() called with: param = [" + Arrays.toString(param.args) + "]");
            }
        });
    }

    @Override
    public boolean validate(Object... args) {

        int i = new Target().returnConst1();
        Log.i(TAG, "return : " + i);

        int j = new Target().returnConst2();
        Log.i(TAG, "return : " + j);
        return (i == 124);
    }
}
