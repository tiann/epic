package me.weishu.epic.samples.tests.arguments;

/**
 * @author weishu
 * @date 17/11/14.
 */

public class ArgStatic88 extends AbsArgStaticCase {

    @Override
    protected void makeCall(long... args) {
        super.makeCall(args);
        ArgumentTarget.arg2(args[0], args[1]);
    }
}
