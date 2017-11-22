package me.weishu.epic.samples.tests.arguments;

/**
 * @author weishu
 * @date 17/11/14.
 */

public class ArgStatic8 extends AbsArgStaticCase {

    @Override
    protected void makeCall(long... args) {
        ArgumentTarget.arg1(args[0]);
    }
}
