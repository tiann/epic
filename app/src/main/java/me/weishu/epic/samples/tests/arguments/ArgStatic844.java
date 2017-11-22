package me.weishu.epic.samples.tests.arguments;

/**
 * Created by weishu on 17/11/14.
 */

public class ArgStatic844 extends AbsArgStaticCase {
    @Override
    protected void makeCall(long... args) {
        ArgumentTarget.arg3(args[0], (int) args[1], (int) args[2]);
    }
}
