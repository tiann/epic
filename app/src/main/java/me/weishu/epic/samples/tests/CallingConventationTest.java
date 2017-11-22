package me.weishu.epic.samples.tests;

import me.weishu.epic.samples.tests.arguments.ArgumentTarget;

/**
 * Created by weishu on 17/11/15.
 */
public class CallingConventationTest {

    public static void longParams1() {
        // r0 = ArtMethod.this
        // r1 = 4
        // r2 = 8
        // r3 = parital 12
        // sp + 16 = partial 12
        ArgumentTarget.arg3(4, 8, 12L);
    }

    public static void longParams2() {
        // r0 = ArtMethod
        // r1, r2 = 1024L
        // r3 = bbcc1122
        // sp + 16 = 0xffaa
        ArgumentTarget.arg2(1024L, 0xffaabbcc1122L);
    }

    public static void longParams3() {
        ArgumentTarget.arg2(123, 0xffaabbcc1122L);
    }
}
