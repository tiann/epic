package me.weishu.epic.samples.tests.custom;

import android.os.Bundle;
import android.util.Log;

import de.robv.android.xposed.DexposedBridge;
import de.robv.android.xposed.XC_MethodHook;
import me.weishu.epic.samples.MainActivity;

/**
 * Created by weishu on 17/11/9.
 */

public class Case8_Activity_onCreate implements Case {
    private static final String TAG = "Case8_Activity_onCreate";

    @Override
    public void hook() {
        DexposedBridge.findAndHookMethod(MainActivity.class, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Log.i(TAG, "before hooked");
                if (param.args[0] == null) {
                    param.args[0] = new Bundle();
                }
                Bundle b = (Bundle) param.args[0];
                Log.i(TAG, "bundle: " + param.args[0]);
                b.putString("hehe", "hacked");
            }
        });
    }

    @Override
    public boolean validate(Object... args) {
        return true;
    }
}
