package me.weishu.epic.samples.tests.custom;

import android.util.Log;

import com.taobao.android.dexposed.XposedHelpers;

import java.lang.reflect.Method;

import me.weishu.epic.art.method.ArtMethod;

/**
 * Created by weishu on 17/11/3.
 */

public class Target {

    public Target() {
    }

    public int test1(Object a, int b) {
        Log.i("mylog", "test1, arg1: " + a + " , arg2:" + b);
        new Runnable() {

            @Override
            public void run() {
                final Method enclosingMethod = getClass().getEnclosingMethod();
                long entry = ArtMethod.of(enclosingMethod).getEntryPointFromQuickCompiledCode();
                if (entry != ArtMethod.getQuickToInterpreterBridge()) {
                    Log.w("mylog", "method compiled....");
                }
                Log.i("mylog", enclosingMethod + "entry: point: 0x" + Long.toHexString(entry));
            }
        }.run();

        return a.hashCode() + b;
    }

    public int test2(int a, int b) {
        return a * b + b * b;
    }

    public int test3(Object a, int b) {
        Log.i("mylog", "test1, arg1: " + a + " , arg2:" + b);
        return a.hashCode() + b;
    }

    public static int test4(int a) {
        return Integer.valueOf(a).hashCode();
    }

    public static int add(int a, int b) {
        return a + b;
    }

    public static float add(int a, float b) {
        return a + b;
    }

    public static int test2(Object a, int b) {
        Log.i("mylog", "test1, arg1: " + a + " , arg2:" + b);

        return a.hashCode() + b;
    }

    public static void validate() {
        final Method validate = XposedHelpers.findMethodExact(Target.class, "validate");

        long entry = ArtMethod.of(validate).getEntryPointFromQuickCompiledCode();
        if (entry != ArtMethod.getQuickToInterpreterBridge()) {
            Log.w("mylog", "method compiled....");
            new Target().test1("123", 1);
        }
        Log.i("mylog", validate + "entry: point: 0x" + Long.toHexString(entry));
    }
}
