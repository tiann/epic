package me.weishu.epic.samples.tests;

import android.util.Log;
import android.widget.Toast;

import me.weishu.epic.samples.MainApplication;

/**
 * Created by weishu on 17/11/13.
 */
public abstract class TestCase {

    private static final String TAG = "TestCase";

    protected String name;
    public TestCase(String name) {
        this.name = name;
    }

    public abstract void test();

    public abstract boolean predicate();

    public boolean validate() {
        boolean validate;
        try {
            validate = predicate();
        } catch (Throwable e) {
            validate = false;
            Log.e(TAG, "error happened:", e);
        }
        if (!validate) {
            Toast.makeText(MainApplication.getAppContext(), "测试不通过。", Toast.LENGTH_SHORT).show();
        }
        return validate;
    }

    public String getName() {
        return name;
    }
}
