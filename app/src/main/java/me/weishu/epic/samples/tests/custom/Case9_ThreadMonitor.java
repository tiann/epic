package me.weishu.epic.samples.tests.custom;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.robv.android.xposed.DexposedBridge;
import de.robv.android.xposed.XC_MethodHook;

/**
 * Created by weishu on 17/11/10.
 */
public class Case9_ThreadMonitor implements Case {

    private static final String TAG = "Case9_ThreadMonitor";

    @Override
    public void hook() {
        try {

            class ThreadMethodHook extends XC_MethodHook {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    Thread t = (Thread) param.thisObject;
                    Log.i(TAG, "thread:" + t + ", started..");
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Thread t = (Thread) param.thisObject;
                    Log.i(TAG, "thread:" + t + ", exit..");
                }
            }

            DexposedBridge.hookAllConstructors(Thread.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Thread thread = (Thread) param.thisObject;
                    Class<?> clazz = thread.getClass();
                    if (clazz != Thread.class) {
                        Log.d(TAG, "found class extend Thread:" + clazz);
                        DexposedBridge.findAndHookMethod(clazz, "run", new ThreadMethodHook());
                    }
                    Log.d(TAG, "Thread: " + thread.getName() + " class:" + thread.getClass() +  " is created.");
                }
            });
            DexposedBridge.findAndHookMethod(Thread.class, "run", new ThreadMethodHook());

        } catch (Throwable e) {
            Log.e(TAG, "hook failed", e);
        }
    }

    @Override
    public boolean validate(Object... args) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "I am started..");
            }
        }).start();

        new MyThread().start();

        final ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < 4; i++) {
            // final int num = i;
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, " lalala");
                }
            });
        }
        return true;
    }

    static class MyThread extends Thread {
        @Override
        public void run() {
            Log.i(TAG, "dang dang dang..");
        }
    }
}
