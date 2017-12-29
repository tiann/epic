package me.weishu.epic.samples.tests.custom;

import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import me.weishu.epic.art.EpicNative;

/**
 * Created by weishu on 17/11/18.
 */

public class Case11_SuspendAll implements Case {

    private static final String TAG = "Case11_SuspendAll";

    @Override
    public void hook() {
        Executor executor = Executors.newCachedThreadPool();
        for (int i = 0; i < 5; i++) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    int j = 0;
                    while (true) {
                        Log.i(TAG, "I am:" + Thread.currentThread().getName() + ", count:" + (j++));
                        SystemClock.sleep(1000);
                        if (j > 3) {
                            break;
                        }
                    }
                }
            });
        }
    }

    @Override
    public boolean validate(Object... args) {
        if (Build.VERSION.SDK_INT < 24) {
            Log.i(TAG, "resume/suspend only support Android N+ now.");
            return false;
        }
        long cookie = EpicNative.suspendAll();
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            Log.i(TAG, thread.getName() + " status:" + thread.getState());
        }
        EpicNative.resumeAll(cookie);

        return true;
    }
}
