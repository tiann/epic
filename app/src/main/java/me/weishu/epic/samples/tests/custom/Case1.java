package me.weishu.epic.samples.tests.custom;

import android.os.SystemClock;
import android.util.Log;

import java.lang.reflect.Field;

import de.robv.android.xposed.DexposedBridge;
import de.robv.android.xposed.XC_MethodHook;

/**
 * Created by weishu on 17/11/6.
 */

public class Case1 implements Case {

    static Field sThread$target;
    static {
        try {
            // Thread#target
            sThread$target = Thread.class.getDeclaredField("target");
            sThread$target.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final String TAG = "Case1";

    @Override
    public void hook() {
        DexposedBridge.findAndHookMethod(Thread.class, "run", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Log.i(TAG, "afterHookedMethod: " + param.thisObject);
                Thread thread = (Thread) param.thisObject;
                Runnable target = (Runnable) sThread$target.get(thread);
                // start|threadName|priority|class|startTime|stacktrace
                Log.i(TAG, "runnable target:" + target);
            }

            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Log.i(TAG, "beforeHookedMethod: " + param.thisObject);

            }
        });
    }

    @Override
    public boolean validate(Object...args) {
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "before sleep");
                SystemClock.sleep(3000);
                Log.i(TAG, "after sleep");
            }
        });
        t1.start();

        class MyThread extends Thread {
            @Override
            public void run() {
                super.run();
                Log.i(TAG, "before sleep");
                SystemClock.sleep(3000);
                Log.i(TAG, "after sleep");
            }
        }

        Thread t2 = new MyThread();
        t2.start();
        return true;
    }
}
