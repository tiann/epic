package epic.weishu.me.epic;

import android.os.Build;
import android.util.Pair;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @auother weishu
 * @date 17/3/6
 */

public class Hook {

    private static Map<Pair<String, String>, Method> sBackups = new ConcurrentHashMap<>();

    public static void hook(Method origin, Method replace) {
        // 1. backup
        Method backUp = backUp(origin, replace);
        sBackups.put(Pair.create(replace.getDeclaringClass().getName(), replace.getName()), backUp);
        // 2. replace method
        Memory.memcpy(MethodInspect.getMethodAddress(origin), MethodInspect.getMethodAddress(replace),
                MethodInspect.getArtMethodSize());
    }

    public static Object callOrigin(Object receiver, Object...params) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement currentStack = stackTrace[3];
        Method method = sBackups.get(Pair.create(currentStack.getClassName(), currentStack.getMethodName()));

        try {
            return method.invoke(receiver, params);
        } catch (Throwable e) {
            throw new UnsupportedException("invoke origin method error", e);
        }
    }

    private static Method backUp(Method origin, Method replace) {
        if (Build.VERSION.SDK_INT < 23) {
            try {
                // java.lang.reflect.ArtMethod
                Class<?> artMethodClass = Class.forName("java.lang.reflect.ArtMethod");
                Field accessFlagsField = artMethodClass.getDeclaredField("accessFlags");
                accessFlagsField.setAccessible(true);
                Constructor<?> artMethodConstructor = artMethodClass.getDeclaredConstructor();
                artMethodConstructor.setAccessible(true);
                Object newArtMethod = artMethodConstructor.newInstance();
                Constructor<Method> methodConstructor = Method.class.getDeclaredConstructor(artMethodClass);
                Method newMethod = methodConstructor.newInstance(newArtMethod);
                newMethod.setAccessible(true);
                Memory.memcpy(MethodInspect.getMethodAddress(newMethod), MethodInspect.getMethodAddress(origin),
                        MethodInspect.getArtMethodSize());

                Integer accessFlags = (Integer) accessFlagsField.get(newArtMethod);
                accessFlags &= ~Modifier.PUBLIC;
                accessFlags |= Modifier.PRIVATE;
                accessFlagsField.set(newArtMethod, accessFlags);

                return newMethod;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else {
        }
        return null;
    }
    private static class Reflection {
        public static Object call(Class<?> clazz, String className, String methodName, Object receiver,
                                  Class[] types, Object[] params) throws UnsupportedException {
            try {
                if (clazz == null) clazz = Class.forName(className);
                Method method = clazz.getDeclaredMethod(methodName, types);
                method.setAccessible(true);
                return method.invoke(receiver, params);
            } catch (Throwable throwable) {
                throw new UnsupportedException("reflection error:", throwable);
            }
        }

        public static Object get(Class<?> clazz, String className, String fieldName, Object receiver) {
            try {
                if (clazz == null) clazz = Class.forName(className);
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(receiver);
            } catch (Throwable e) {
                throw new UnsupportedException("reflection error:", e);
            }
        }
    }

    public static class MethodInspect {

        static long sMethodSize = -1;

        public static void ruler1() {
        }

        public static void ruler2() {
        }

        public static long getMethodAddress(Method method) {

            Object mirrorMethod = Reflection.get(Method.class.getSuperclass(), null, "artMethod", method);
            if (mirrorMethod.getClass().equals(Long.class)) {
                return (Long) mirrorMethod;
            }
            return Unsafe.getObjectAddress(mirrorMethod);
        }

        public static long getArtMethodSize() {
            if (sMethodSize > 0) {
                return sMethodSize;
            }

            try {
                Method f1 = MethodInspect.class.getDeclaredMethod("ruler1");
                Method f2 = MethodInspect.class.getDeclaredMethod("ruler2");
                sMethodSize = getMethodAddress(f2) - getMethodAddress(f1);
                return sMethodSize;
            } catch (Exception e) {
                throw new RuntimeException("exceuse me ?? can not found method??");
            }
        }

        public static byte[] getMethodBytes(Method method) {
            if (method == null) {
                return null;
            }
            byte[] ret = new byte[(int) getArtMethodSize()];
            long baseAddr = getMethodAddress(method);
            for (int i = 0; i < ret.length; i++) {
                ret[i] = Memory.peekByte(baseAddr + i);
            }
            return ret;
        }
    }

    private static class Memory {

        // libcode.io.Memory#peekByte
        static byte peekByte(long address) {
            return (Byte) Reflection.call(null, "libcore.io.Memory", "peekByte", null, new Class[]{long.class}, new Object[]{address});
        }

        static void pokeByte(long address, byte value) {
            Reflection.call(null, "libcore.io.Memory", "pokeByte", null, new Class[]{long.class, byte.class}, new Object[]{address, value});
        }

        public static void memcpy(long dst, long src, long length) {
            for (long i = 0; i < length; i++) {
                pokeByte(dst, peekByte(src));
                dst++;
                src++;
            }
        }
    }

    static class Unsafe {

        static final String UNSAFE_CLASS = "sun.misc.Unsafe";
        static Object THE_UNSAFE = Reflection.get(null, UNSAFE_CLASS, "THE_ONE", null);

        public static long getObjectAddress(Object o) {
            Object[] objects = {o};
            Integer baseOffset = (Integer) Reflection.call(null, UNSAFE_CLASS,
                    "arrayBaseOffset", THE_UNSAFE, new Class[]{Class.class}, new Object[]{Object[].class});
            return ((Number) Reflection.call(null, UNSAFE_CLASS, "getInt", THE_UNSAFE,
                    new Class[]{Object.class, long.class}, new Object[]{objects, baseOffset.longValue()})).longValue();
        }
    }

    private static class UnsupportedException extends RuntimeException {
        UnsupportedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
