package me.weishu.epic.samples.tests;

import android.util.Log;

import java.util.Arrays;

import de.robv.android.xposed.XC_MethodHook;

/**
 * Created by weishu on 17/11/13.
 */

public class LogMethodHook extends XC_MethodHook {

    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        super.beforeHookedMethod(param);
        Log.i(getClass().getSimpleName(), "beforeHookedMethod() called with: param = [" + paramToString(param) + "]");
    }

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        super.afterHookedMethod(param);
        Log.i(getClass().getSimpleName(), "afterHookedMethod() called with: param = [" + paramToString(param) + "]");
    }

    private static String paramToString(MethodHookParam param) {
        StringBuilder sb = new StringBuilder(param.getClass().getSimpleName()).append("{");
        sb.append("method = ").append(param.method.getName()).append(", ");
        sb.append("this = ").append(param.thisObject).append(", ");
        sb.append("args = ").append(Arrays.toString(param.args)).append(",");
        sb.append("result = ").append(param.getResult()).append("}");
        return sb.toString();
    }
}
