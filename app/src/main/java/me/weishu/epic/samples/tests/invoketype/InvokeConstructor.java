package me.weishu.epic.samples.tests.invoketype;

import de.robv.android.xposed.DexposedBridge;
import de.robv.android.xposed.XposedHelpers;
import me.weishu.epic.samples.tests.LogMethodHook;
import me.weishu.epic.samples.tests.TestCase;

/**
 * Created by weishu on 17/11/14.
 */

public class InvokeConstructor extends TestCase {

    boolean callBefore = false;
    boolean callAfter = false;
    public InvokeConstructor() {
        super("Constructor");
    }

    @Override
    public void test() {
        DexposedBridge.hookMethod(XposedHelpers.findConstructorExact(InvokeTypeTarget.class), new LogMethodHook() {
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
        new InvokeTypeTarget();
        return callBefore && callAfter;
    }
}
