package me.weishu.epic.samples.tests.arguments;


/**
 * Created by weishu on 17/11/14.
 */
public class ArgStatic8844 extends AbsArgStaticCase {
    @Override
    protected void makeCall(long... args) {
        ArgumentTarget.arg4(args[0], args[1], (int)args[2], (int)args[3]);
    }
}
