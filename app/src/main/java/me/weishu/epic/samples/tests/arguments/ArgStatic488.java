package me.weishu.epic.samples.tests.arguments;

/**
 * Created by weishu on 17/11/14.
 */

public class ArgStatic488 extends AbsArgStaticCase {
    @Override
    protected void makeCall(long... args) {
        ArgumentTarget.arg3((int) args[0], args[1], args[2]);
    }
}
