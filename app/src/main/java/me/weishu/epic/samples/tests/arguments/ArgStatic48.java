package me.weishu.epic.samples.tests.arguments;

/**
 * @author weishu
 * @date 17/11/14.
 */

public class ArgStatic48 extends AbsArgStaticCase {

    @Override
    protected void makeCall(long... args) {
        ArgumentTarget.arg2((int) args[0], args[1]);
    }
}
