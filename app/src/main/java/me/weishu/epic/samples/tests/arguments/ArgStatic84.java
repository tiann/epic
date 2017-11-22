package me.weishu.epic.samples.tests.arguments;

/**
 * @author weishu
 * @date 17/11/14.
 */

public class ArgStatic84 extends AbsArgStaticCase {

    @Override
    protected void makeCall(long... args) {
        ArgumentTarget.arg2(args[0], (int)args[1]);
    }
}
