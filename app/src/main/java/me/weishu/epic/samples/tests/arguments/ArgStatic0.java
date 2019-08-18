package me.weishu.epic.samples.tests.arguments;

import de.robv.android.xposed.DexposedBridge;
import de.robv.android.xposed.XC_MethodHook;
import me.weishu.epic.samples.tests.LogMethodHook;
import me.weishu.epic.samples.tests.TestCase;

/**
 * @author weishu
 * @date 17/11/14.
 */

public class ArgStatic0 extends TestCase {

    boolean beforeCalled = false;
    boolean afterCalled = false;
    public ArgStatic0() {
        super("ArgStatic0");
    }

    @Override
    public void test() {
        DexposedBridge.findAndHookMethod(ArgumentTarget.class, "arg0", new LogMethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                beforeCalled = true;
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                afterCalled = true;
            }
        });
    }

    @Override
    public boolean predicate() {

        ArgumentTarget.arg0();

        return beforeCalled && afterCalled;
    }

}
