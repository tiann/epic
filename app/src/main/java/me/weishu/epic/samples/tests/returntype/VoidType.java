package me.weishu.epic.samples.tests.returntype;

import android.util.Log;

import de.robv.android.xposed.DexposedBridge;
import de.robv.android.xposed.XC_MethodHook;
import me.weishu.epic.samples.tests.TestCase;

/**
 * Created by weishu on 17/11/13.
 */

public class VoidType extends TestCase {

    private static final String TAG = "VoidType";

    boolean callBefore = false;
    boolean callAfter = false;

    public VoidType() {
        super("无返回值");
    }

    @Override
    public void test() {
        DexposedBridge.findAndHookMethod(ReturnTypeTarget.class, "returnVoid", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                callBefore = true;
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                callAfter = true;
            }
        });
    }

    @Override
    public boolean predicate() {
        ReturnTypeTarget.returnVoid();

        Log.i(TAG, "callBefore:" + callBefore + ", callAfter:" + callAfter);
        return callBefore && callAfter;
    }

}
