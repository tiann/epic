package me.weishu.epic.samples.tests.arguments;

import android.util.Log;

/**
 * @author weishu
 * @date 17/11/13.
 */

public class ArgumentTarget {

    private static final String TAG = "ArgumentTarget";

    //region ---------------static---------------

    public static void arg0() {
        Log.i(TAG, "arg0() called");
    }

    public static void arg1(int v1) {
        Log.i(TAG, "arg1() called with: v1 = [" + v1 + "]");
    }

    public static void arg1(long v1) {
        Log.i(TAG, "arg1() called with: v1 = [" + v1 + "]");
    }

    public static void arg2(int v1, int v2) {
        Log.i(TAG, "arg2() called with: v1 = [" + v1 + "], v2 = [" + v2 + "]");
    }

    public static void arg2(int v1, long v2) {
        Log.i(TAG, "arg2() called with: v1 = [" + v1 + "], v2 = [" + v2 + "]");
    }

    public static void arg2(long v1, int v2) {
        Log.i(TAG, "arg2() called with: v1 = [" + v1 + "], v2 = [" + v2 + "]");
    }

    public static void arg2(long v1, long v2) {
        Log.i(TAG, "arg2() called with: v1 = [" + v1 + "], v2 = [" + v2 + "]");
    }

    public static void arg3(int v1, int v2, int v3) {
        Log.i(TAG, "arg3() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "]");
    }

    public static void arg3(long v1, int v2, int v3) {
        Log.i(TAG, "arg3() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "]");
    }

    public static void arg3(int v1, long v2, int v3) {
        Log.i(TAG, "arg3() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "]");
    }

    public static void arg3(int v1, int v2, long v3) {
        Log.i(TAG, "arg3() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "]");
    }

    public static void arg3(long v1, long v2, int v3) {
        Log.i(TAG, "arg3() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "]");
    }

    public static void arg3(long v1, int v2, long v3) {
        Log.i(TAG, "arg3() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "]");
    }

    public static void arg3(int v1, long v2, long v3) {
        Log.i(TAG, "arg3() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "]");
    }

    public static void arg3(long v1, long v2, long v3) {
        Log.i(TAG, "arg3() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "]");
    }

    public static void arg4(int v1, int v2, int v3, int v4) {
        Log.i(TAG, "arg4() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "], v4 = [" + v4 + "]");
    }

    public static void arg4(long v1, int v2, int v3, int v4) {
        Log.i(TAG, "arg4() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "], v4 = [" + v4 + "]");
    }

    public static void arg4(int v1, long v2, int v3, int v4) {
        Log.i(TAG, "arg4() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "], v4 = [" + v4 + "]");
    }

    public static void arg4(int v1, int v2, long v3, int v4) {
        Log.i(TAG, "arg4() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "], v4 = [" + v4 + "]");
    }

    public static void arg4(int v1, int v2, int v3, long v4) {
        Log.i(TAG, "arg4() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "], v4 = [" + v4 + "]");
    }

    public static void arg4(long v1, long v2, int v3, int v4) {
        Log.i(TAG, "arg4() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "], v4 = [" + v4 + "]");
    }

    public static void arg4(long v1, int v2, long v3, int v4) {
        Log.i(TAG, "arg4() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "], v4 = [" + v4 + "]");
    }

    public static void arg4(long v1, int v2, int v3, long v4) {
        Log.i(TAG, "arg4() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "], v4 = [" + v4 + "]");
    }

    public static void arg4(int v1, long v2, long v3, int v4) {
        Log.i(TAG, "arg4() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "], v4 = [" + v4 + "]");
    }

    public static void arg4(int v1, long v2, int v3, long v4) {
        Log.i(TAG, "arg4() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "], v4 = [" + v4 + "]");
    }

    public static void arg4(int v1, int v2, long v3, long v4) {
        Log.i(TAG, "arg4() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "], v4 = [" + v4 + "]");
    }

    public static void arg4(int v1, long v2, long v3, long v4) {
        Log.i(TAG, "arg4() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "], v4 = [" + v4 + "]");
    }

    public static void arg4(long v1, int v2, long v3, long v4) {
        Log.i(TAG, "arg4() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "], v4 = [" + v4 + "]");
    }

    public static void arg4(long v1, long v2, int v3, long v4) {
        Log.i(TAG, "arg4() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "], v4 = [" + v4 + "]");
    }

    public static void arg4(long v1, long v2, long v3, int v4) {
        Log.i(TAG, "arg4() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "], v4 = [" + v4 + "]");
    }

    public static void arg4(long v1, long v2, long v3, long v4) {
        Log.i(TAG, "arg4() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "], v4 = [" + v4 + "]");
    }

    public static void arg5(int v1, int v2, int v3, int v4, int v5) {
        Log.i(TAG, "arg5() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "], v4 = [" + v4 + "], v5 = [" + v5 + "]");
    }

    public static void arg5(long v1, long v2, long v3, long v4, long v5) {
        Log.i(TAG, "arg5() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "], v4 = [" + v4 + "], v5 = [" + v5 + "]");
    }

    public static void arg6(int v1, int v2, int v3, int v4, int v5, int v6) {
        Log.i(TAG, "arg6() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "], v4 = [" + v4 + "], v5 = [" + v5 + "], v6 = [" + v6 + "]");
    }

    public static void arg6(long v1, long v2, long v3, long v4, long v5, long v6) {
        Log.i(TAG, "arg6() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "], v4 = [" + v4 + "], v5 = [" + v5 + "], v6 = [" + v6 + "]");
    }

    public static void arg7(int v1, int v2, int v3, int v4, int v5, int v6, int v7) {
        Log.i(TAG, "arg7() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "], v4 = [" + v4 + "], v5 = [" + v5 + "], v6 = [" + v6 + "], v7 = [" + v7 + "]");
    }

    public static void arg7(long v1, long v2, long v3, long v4, long v5, long v6, long v7) {
        Log.i(TAG, "arg7() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "], v4 = [" + v4 + "], v5 = [" + v5 + "], v6 = [" + v6 + "], v7 = [" + v7 + "]");
    }

    //endregion

    //region ---------------non static---------------

    public void iarg0() {
        Log.i(TAG, "arg0() called");
    }

    public void iarg1(int v1) {
        Log.i(TAG, "arg1() called with: v1 = [" + v1 + "]");
    }

    public void iarg1(long v1) {
        Log.i(TAG, "arg1() called with: v1 = [" + v1 + "]");
    }

    public void iarg2(int v1, int v2) {
        Log.i(TAG, "arg2() called with: v1 = [" + v1 + "], v2 = [" + v2 + "]");
    }

    public void iarg2(int v1, long v2) {
        Log.i(TAG, "arg2() called with: v1 = [" + v1 + "], v2 = [" + v2 + "]");
    }

    public void iarg2(long v1, long v2) {
        Log.i(TAG, "arg2() called with: v1 = [" + v1 + "], v2 = [" + v2 + "]");
    }

    public void iarg3(int v1, int v2, int v3) {
        Log.i(TAG, "arg3() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "]");
    }

    public void iarg3(long v1, int v2, int v3) {
        Log.i(TAG, "arg3() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "]");
    }

    public void iarg3(int v1, long v2, int v3) {
        Log.i(TAG, "arg3() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "]");
    }

    public void iarg3(int v1, int v2, long v3) {
        Log.i(TAG, "arg3() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "]");
    }

    public void iarg3(long v1, long v2, int v3) {
        Log.i(TAG, "arg3() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "]");
    }

    public void iarg3(long v1, int v2, long v3) {
        Log.i(TAG, "arg3() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "]");
    }

    public void iarg3(int v1, long v2, long v3) {
        Log.i(TAG, "arg3() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "]");
    }

    public void iarg3(long v1, long v2, long v3) {
        Log.i(TAG, "arg3() called with: v1 = [" + v1 + "], v2 = [" + v2 + "], v3 = [" + v3 + "]");
    }

    //endregion
}
