package me.weishu.epic.samples.tests.custom;

import android.util.Log;

import de.robv.android.xposed.DexposedBridge;
import de.robv.android.xposed.XC_MethodHook;

/**
 * Created by weishu on 17/11/21.
 */
public class Case12_MultiCallback implements Case {
    private static final String TAG = "Case12_MultiCallback";

    int beforeCount = 0;
    int afterCount = 0;

    XC_MethodHook callback1 = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            super.beforeHookedMethod(param);
            Log.i(TAG, "beforeHookMethod 1");
            beforeCount++;
        }

        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            super.afterHookedMethod(param);
            Log.i(TAG, "afterHookMethod 1");
            afterCount++;
        }
    };

    XC_MethodHook callback2 = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            super.beforeHookedMethod(param);
            Log.i(TAG, "beforeHookMethod 2 lalala ");
            beforeCount++;
        }

        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            super.afterHookedMethod(param);
            Log.i(TAG, "afterHookMethod 2 lalala");
            afterCount++;
        }
    };

    XC_MethodHook callback3 = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            super.beforeHookedMethod(param);
            Log.i(TAG, "beforeHookMethod 3 zezeze");
            beforeCount++;
        }

        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            super.afterHookedMethod(param);
            Log.i(TAG, "afterHookMethod 3 zezeze");
            afterCount++;
        }
    };

    @Override
    public void hook() {
        DexposedBridge.findAndHookMethod(Target.class, "test1", Object.class, int.class, callback1);
        DexposedBridge.findAndHookMethod(Target.class, "test1", Object.class, int.class, callback2);
        DexposedBridge.findAndHookMethod(Target.class, "test1", Object.class, int.class, callback3);
    }

    @Override
    public boolean validate(Object... args) {
        new Target().test1("123", 1);
        boolean ret = beforeCount == 3 && afterCount == 3;
        // reset.
        beforeCount = 0;
        afterCount = 0;

        return ret;
    }
}
