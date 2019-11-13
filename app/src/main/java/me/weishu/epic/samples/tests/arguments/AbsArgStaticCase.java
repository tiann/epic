package me.weishu.epic.samples.tests.arguments;

import android.util.Log;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Random;

import de.robv.android.xposed.DexposedBridge;
import de.robv.android.xposed.XposedHelpers;
import me.weishu.epic.samples.tests.LogMethodHook;
import me.weishu.epic.samples.tests.TestCase;

/**
 * @author weishu
 * @date 17/11/14.
 */

public abstract class AbsArgStaticCase extends TestCase {

    private final String TAG = getClass().getSimpleName();

    final long[] args;

    private Random r = new Random();

    public AbsArgStaticCase() {
        super(null);
        args = new long[8];
        for (int i = 0; i < args.length; i++) {
            args[i] = 0L;
        }

        name = getClass().getSimpleName();
    }

    @Override
    public void test() {
        DexposedBridge.hookMethod(getTargetMethod(), new LogMethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                int length = param.args.length;
                if (length > 0) {
                    for (int i = 0; i < length; i++) {
                        long tmp = ((Number) param.args[i]).longValue();
                        args[i] = tmp;
                    }
                }
            }
        });
    }

    @Override
    public boolean predicate() {
        long[] arguments = getArguments();
        Log.i(TAG, "call arguments: " + Arrays.toString(toHex(arguments)));

        makeCall(arguments);

        boolean ret = true;

        int length = getArgumentNumber();
        for (int i = 0; i < length; i++) {
            if (arguments[i] != args[i]) {
                ret = false;
                Log.i(TAG, "hooked arguments: " + Arrays.toString(toHex(args)));
            }
        }
        return ret;
    }

    private long[] getArguments() {
        final int argumentNumber = getArgumentNumber();
        long[] arguments = new long[argumentNumber];
        for (int i = 0; i < arguments.length; i++) {
            arguments[i] = nextLong();
        }
        return arguments;
    }

    protected Method getTargetMethod() {
        final int argumentNumber = getArgumentNumber();
        String methodName = "arg" + argumentNumber;

        Method method = XposedHelpers.findMethodExact(ArgumentTarget.class, methodName, getParamsSignature());
        Log.i(TAG, "find target Method:" + method);
        return method;
    }

    protected int getArgumentNumber() {
        return getParamsSignature().length;
    }

    protected void makeCall(long... args) {
        Log.i(TAG, getName() + " make call with arguments:" + Arrays.toString(args));
    }

    private long nextLong() {
        int ret = r.nextInt();
        return ret;
    }

    private Class<?>[] getParamsSignature() {
        String className = getClass().getSimpleName();
        String signature = className.substring("ArgStatic".length());
        final int length = signature.length();
        Class<?>[] clazz = new Class<?>[length];
        for (int i = 0; i < length; i++) {
            final char c = signature.charAt(i);
            if (c == '4') {
                clazz[i] = int.class;
            } else if (c == '8') {
                clazz[i] = long.class;
            } else {
                throw new RuntimeException("Unknown signature!!");
            }
        }
        return clazz;
    }

    private String[] toHex(long[] value) {
        String[] ret = new String[value.length];

        for (int i = 0; i < value.length; i++) {
            ret[i] = Long.toHexString(value[i]);
        }
        return ret;
    }
}
