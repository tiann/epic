package me.weishu.epic.samples.tests.returntype;

import de.robv.android.xposed.DexposedBridge;

import me.weishu.epic.samples.tests.LogMethodHook;
import me.weishu.epic.samples.tests.TestCase;

/**
 * Created by weishu on 17/11/13.
 */
public class FloatType extends TestCase {

    final float returnType = 12545.212f;
    final float returnTypeModified = returnType - 1;

    public FloatType() {
        super("Float");
    }

    @Override
    public void test() {


        DexposedBridge.findAndHookMethod(ReturnTypeTarget.class, "returnFloat", float.class, new LogMethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(returnTypeModified);
                super.beforeHookedMethod(param);
            }
        });
    }

    @Override
    public boolean predicate() {
        return ReturnTypeTarget.returnFloat(returnType) == returnTypeModified;
    }
}
