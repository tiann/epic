package me.weishu.epic.samples.tests.returntype;

import android.util.Log;

/**
 * Created by weishu on 17/11/13.
 */

public class ReturnTypeTarget {
    private static final String TAG = "ReturnTypeTarget";

    public static void returnVoid() {
        Log.d(TAG, "returnVoid() called");
    }

    public static byte returnByte(byte b) {
        Log.i(TAG, "returnByte() called");
        return b;
    }

    public static char returnChar(char c) {
        Log.i(TAG, "returnChar() called");
        return c;
    }

    public static short returnShort(short s) {
        Log.i(TAG, "returnShort() called");
        return s;
    }

    public static int returnInt(int i) {
        Log.i(TAG, "returnInt() called");
        return i;
    }

    public static long returnLong(long l) {
        Log.i(TAG, "returnLong() called");
        return l;
    }

    public static float returnFloat(float f) {
        Log.i(TAG, "returnFloat() called");
        return f;
    }

    public static double returnDouble(double d) {
        Log.i(TAG, "returnDouble() called");
        return d;
    }

    public static boolean returnBoolean(boolean b) {
        Log.i(TAG, "returnBoolean() called");
        return b;
    }

    public static String returnString(String s) {
        Log.i(TAG, "returnString() called with: s = [" + s + "]");
        return s;
    }

    public static String[] returnStringArray(String[] a) {
        Log.i(TAG, "returnStringArray() called with: a = [" + a + "]");
        return a;
    }

    public static ReturnTypeTarget returnCustom(ReturnTypeTarget r) {
        Log.i(TAG, "returnCustom() called with: r = [" + r + "]");
        return r;
    }
}
