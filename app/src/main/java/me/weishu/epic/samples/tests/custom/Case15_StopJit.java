package me.weishu.epic.samples.tests.custom;

import me.weishu.epic.art.EpicNative;

/**
 * Created by weishu on 17/12/13.
 */

public class Case15_StopJit implements Case {
    long cookie;
    @Override
    public void hook() {
        cookie = EpicNative.stopJit();
    }

    @Override
    public boolean validate(Object... args) {
        EpicNative.startJit(cookie);
        return true;
    }
}
