package me.weishu.epic.samples.tests.arguments;

import android.util.Log;

import java.util.Arrays;

/**
 * Created by weishu on 17/11/14.
 */

public class ArgStatic444 extends AbsArgStaticCase {

    @Override
    protected void makeCall(long... args) {
        Log.i("mylog", "make call args:" + Arrays.toString(args));
        ArgumentTarget.arg3((int) args[0], (int) args[1], (int) args[2]);
    }
}
