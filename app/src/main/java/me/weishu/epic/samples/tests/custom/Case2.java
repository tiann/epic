package me.weishu.epic.samples.tests.custom;

/**
 * Created by weishu on 17/11/6.
 */

public class Case2 implements Case{
    private static final String TAG = "Case2";

    @Override
    public void hook() {
//        DexposedBridge.findAndHookMethod(Target.class, "add", int.class, int.class, new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                Log.i(TAG, "before add hooked:" + Arrays.toString(param.args));
//                param.setResult(4);
//                super.beforeHookedMethod(param);
//            }
//        });
//
//        DexposedBridge.findAndHookMethod(Target.class, "test4", int.class, new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                super.beforeHookedMethod(param);
//                Log.i("mylog", "before", new RuntimeException("stack"));
//                Log.i("mylog", "this:" + param.thisObject);
//                Log.i("mylog", "method:" + param.method);
//                Log.i("mylog", "args:" + Arrays.toString(param.args));
//
//            }
//
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                super.afterHookedMethod(param);
//                Log.i("mylog", "after");
//            }
//        });
    }

    @Override
    public boolean validate(Object... args) {
//        Log.i(TAG, "1 + 2: " + Target.add(1, 2));
//        return Target.add(1, 2) == 4;
        Target.validate();
        return true;
    }
}
