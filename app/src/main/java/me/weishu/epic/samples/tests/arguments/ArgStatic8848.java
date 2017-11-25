package me.weishu.epic.samples.tests.arguments;


/**
 * Created by weishu on 17/11/14.
 */
public class ArgStatic8848 extends AbsArgStaticCase {
    @Override
    protected void makeCall(long... args) {
        ArgumentTarget.arg4(args[0], args[1], (int)args[2], args[3]);
    }
}
