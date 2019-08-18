package me.weishu.epic.samples.tests.custom;

import android.os.SystemClock;
import android.util.Log;

import java.lang.reflect.Method;

import de.robv.android.xposed.XposedHelpers;
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

    public int returnConst(int a) {
        return a;
    }

    public int add(int a, int b) {
        return a + b;
    }

    public int plus(int a, int b) {
        return a + b;
    }

    public int test3(Object a, int b) {
        Log.i("mylog", "test1, arg1: " + a + " , arg2:" + b);
        return a.hashCode() + b;
    }

    public static int test4(int a) {
        return Integer.valueOf(a).hashCode();
    }


    public static float add(int a, float b) {
        return a + b;
    }

    public static int test2(Object a, int b) {
        Log.i("mylog", "test1, arg1: " + a + " , arg2:" + b);

        return a.hashCode() + b;
    }

    public long longRunMethod() {
        SystemClock.sleep(4000);
        return SystemClock.elapsedRealtime();
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
