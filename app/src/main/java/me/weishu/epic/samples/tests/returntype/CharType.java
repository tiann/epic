package me.weishu.epic.samples.tests.returntype;

import de.robv.android.xposed.DexposedBridge;

import me.weishu.epic.samples.tests.LogMethodHook;
import me.weishu.epic.samples.tests.TestCase;

/**
 * Created by weishu on 17/11/13.
 */
public class CharType extends TestCase {

    final char returnType = Character.MAX_VALUE;
    final char returnTypeModified = returnType - 1;

    public CharType() {
        super("Char");
    }

    @Override
    public void test() {


        DexposedBridge.findAndHookMethod(ReturnTypeTarget.class, "returnChar", char.class, new LogMethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(returnTypeModified);
                super.beforeHookedMethod(param);
            }
        });
    }

    @Override
    public boolean predicate() {
        return ReturnTypeTarget.returnChar(returnType) == returnTypeModified;
    }
}
