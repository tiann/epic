package me.weishu.epic.samples.tests.returntype;

import de.robv.android.xposed.DexposedBridge;

import me.weishu.epic.samples.tests.LogMethodHook;
import me.weishu.epic.samples.tests.TestCase;

/**
 * Created by weishu on 17/11/13.
 */
public class IntType extends TestCase {

    final int returnType = Integer.MAX_VALUE;
    final int returnTypeModified = returnType - 1;

    public IntType() {
        super("Int");
    }

    @Override
    public void test() {


        DexposedBridge.findAndHookMethod(ReturnTypeTarget.class, "returnInt", int.class, new LogMethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(returnTypeModified);
                super.beforeHookedMethod(param);
            }
        });
    }

    @Override
    public boolean predicate() {
        final int i = ReturnTypeTarget.returnInt(returnType);
        return i == returnTypeModified;
    }
}
