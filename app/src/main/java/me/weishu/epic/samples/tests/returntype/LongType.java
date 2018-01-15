package me.weishu.epic.samples.tests.returntype;

import de.robv.android.xposed.DexposedBridge;

import me.weishu.epic.samples.tests.LogMethodHook;
import me.weishu.epic.samples.tests.TestCase;

/**
 * Created by weishu on 17/11/13.
 */
public class LongType extends TestCase {

    final long returnType = Long.MAX_VALUE / 2;
    final long returnTypeModified = returnType - 1;

    public LongType() {
        super("Long");
    }

    @Override
    public void test() {


        DexposedBridge.findAndHookMethod(ReturnTypeTarget.class, "returnLong", long.class, new LogMethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(returnTypeModified);
                super.beforeHookedMethod(param);
            }
        });
    }

    @Override
    public boolean predicate() {
        return ReturnTypeTarget.returnLong(returnType) == returnTypeModified;
    }
}
