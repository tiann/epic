/*
 * Copyright (c) 2017, weishu twsxtd@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.weishu.epic.art.entry;

import android.util.Log;

import com.taobao.android.dexposed.DexposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.android.dexposed.utility.Logger;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import me.weishu.epic.art.Epic;
import me.weishu.epic.art.EpicNative;

@SuppressWarnings({"unused", "ConstantConditions"})
public class Entry64 {

    private final static String TAG = "Entry64";

    //region ---------------callback---------------
    private static int onHookInt(Object artmethod, Object receiver, Object[] args) {
        return (Integer) DexposedBridge.handleHookedArtMethod(artmethod, receiver, args);
    }

    private static long onHookLong(Object artmethod, Object receiver, Object[] args) {
        return (Long) DexposedBridge.handleHookedArtMethod(artmethod, receiver, args);
    }

    private static double onHookDouble(Object artmethod, Object receiver, Object[] args) {
        return (Double) DexposedBridge.handleHookedArtMethod(artmethod, receiver, args);
    }

    private static char onHookChar(Object artmethod, Object receiver, Object[] args) {
        return (Character) DexposedBridge.handleHookedArtMethod(artmethod, receiver, args);
    }

    private static short onHookShort(Object artmethod, Object receiver, Object[] args) {
        return (Short) DexposedBridge.handleHookedArtMethod(artmethod, receiver, args);
    }

    private static float onHookFloat(Object artmethod, Object receiver, Object[] args) {
        return (Float) DexposedBridge.handleHookedArtMethod(artmethod, receiver, args);
    }

    private static Object onHookObject(Object artmethod, Object receiver, Object[] args) {
        return DexposedBridge.handleHookedArtMethod(artmethod, receiver, args);
    }

    private static void onHookVoid(Object artmethod, Object receiver, Object[] args) {
        DexposedBridge.handleHookedArtMethod(artmethod, receiver, args);
    }

    private static boolean onHookBoolean(Object artmethod, Object receiver, Object[] args) {
        return (Boolean) DexposedBridge.handleHookedArtMethod(artmethod, receiver, args);
    }

    private static byte onHookByte(Object artmethod, Object receiver, Object[] args) {
        return (Byte) DexposedBridge.handleHookedArtMethod(artmethod, receiver, args);
    }
    //endregion

    //region ---------------bridge---------------
    private static void voidBridge(long r1, long self, long struct, long x4, long x5, long x6, long x7) {
         referenceBridge(r1, self, struct, x4, x5, x6, x7);
    }

    private static boolean booleanBridge(long r1, long self, long struct, long x4, long x5, long x6, long x7) {
        return (Boolean) referenceBridge(r1, self, struct, x4, x5, x6, x7);
    }

    private static byte byteBridge(long r1, long self, long struct, long x4, long x5, long x6, long x7) {
        return (Byte) referenceBridge(r1, self, struct, x4, x5, x6, x7);
    }

    private static short shortBridge(long r1, long self, long struct, long x4, long x5, long x6, long x7) {
        return (Short) referenceBridge(r1, self, struct, x4, x5, x6, x7);
    }

    private static char charBridge(long r1, long self, long struct, long x4, long x5, long x6, long x7) {
        return (Character) referenceBridge(r1, self, struct, x4, x5, x6, x7);
    }

    private static int intBridge(long r1, long self, long struct, long x4, long x5, long x6, long x7) {
        return (Integer) referenceBridge(r1, self, struct, x4, x5, x6, x7);
    }

    private static long longBridge(long r1, long self, long struct, long x4, long x5, long x6, long x7) {
        return (Long) referenceBridge(r1, self, struct, x4, x5, x6, x7);
    }

    private static float floatBridge(long r1, long self, long struct, long x4, long x5, long x6, long x7) {
        return (Float) referenceBridge(r1, self, struct, x4, x5, x6, x7);
    }

    private static double doubleBridge(long r1, long self, long struct, long x4, long x5, long x6, long x7) {
        return (Double) referenceBridge(r1, self, struct, x4, x5, x6, x7);
    }
    //endregion

    private static Object referenceBridge(long x1, long self, long struct, long x4, long x5, long x6, long x7) {
        Logger.w(TAG, "enter bridge function.");

        // struct {
        //     void* sp;
        //     void* r2;
        //     void* r3;
        //     void* sourceMethod
        // }
        // sp + 16 = r4

        Logger.d(TAG, "self:" + Long.toHexString(self));
        final long nativePeer = XposedHelpers.getLongField(Thread.currentThread(), "nativePeer");
        Logger.d(TAG, "java thread native peer:" + Long.toHexString(nativePeer));

        Logger.d(TAG, "struct:" + Long.toHexString(struct));

        final long sp = ByteBuffer.wrap(EpicNative.get(struct, 8)).order(ByteOrder.LITTLE_ENDIAN).getLong();

        Logger.d(TAG, "stack:" + sp);

        final byte[] rr1 = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(x1).array();
        final byte[] r2 = EpicNative.get(struct + 8, 8);

        final byte[] r3 = EpicNative.get(struct + 16, 8);

        final long sourceMethod = ByteBuffer.wrap(EpicNative.get(struct + 24, 8)).order(ByteOrder.LITTLE_ENDIAN).getLong();
        Logger.d(TAG, "sourceMethod:" + Long.toHexString(sourceMethod));

        Epic.MethodInfo originMethodInfo = Epic.getMethodInfo(sourceMethod);
        Logger.d(TAG, "originMethodInfo :" + originMethodInfo);

        boolean isStatic = originMethodInfo.isStatic;

        int numberOfArgs = originMethodInfo.paramNumber;
        Class<?>[] typeOfArgs = originMethodInfo.paramTypes;
        Object[] arguments = new Object[numberOfArgs];

        Object receiver;

        self = nativePeer;

        if (isStatic) {
            receiver = null;
            do {
                if (numberOfArgs == 0) break;
                arguments[0] = wrapArgument(typeOfArgs[0], self, rr1);
                if (numberOfArgs == 1) break;
                arguments[1] = wrapArgument(typeOfArgs[1], self, r2);
                if (numberOfArgs == 2) break;
                arguments[2] = wrapArgument(typeOfArgs[2], self, r3);
                if (numberOfArgs == 3) break;
                arguments[3] = wrapArgument(typeOfArgs[3], self, x4);
                if (numberOfArgs == 4) break;
                arguments[4] = wrapArgument(typeOfArgs[4], self, x5);
                if (numberOfArgs == 5) break;
                arguments[5] = wrapArgument(typeOfArgs[5], self, x6);
                if (numberOfArgs == 6) break;
                arguments[6] = wrapArgument(typeOfArgs[6], self, x7);
                if (numberOfArgs == 7) break;

                for (int i = 7; i < numberOfArgs; i++) {
                    byte[] argsInStack = EpicNative.get(sp + i * 8 + 8, 8);
                    arguments[i] = wrapArgument(typeOfArgs[i], self, argsInStack);
                }
            } while (false);

        } else {

            receiver = EpicNative.getObject(self, x1);
            Logger.i(TAG, "this :" + receiver);

            do {
                if (numberOfArgs == 0) break;
                arguments[0] = wrapArgument(typeOfArgs[0], self, r2);
                if (numberOfArgs == 1) break;
                arguments[1] = wrapArgument(typeOfArgs[1], self, r3);
                if (numberOfArgs == 2) break;
                arguments[2] = wrapArgument(typeOfArgs[2], self, x4);
                if (numberOfArgs == 3) break;
                arguments[3] = wrapArgument(typeOfArgs[3], self, x5);
                if (numberOfArgs == 4) break;
                arguments[4] = wrapArgument(typeOfArgs[4], self, x6);
                if (numberOfArgs == 5) break;
                arguments[5] = wrapArgument(typeOfArgs[5], self, x7);
                if (numberOfArgs == 6) break;

                for (int i = 6; i < numberOfArgs; i++) {
                    byte[] argsInStack = EpicNative.get(sp + i * 8 + 16, 8);
                    arguments[i] = wrapArgument(typeOfArgs[i], self, argsInStack);
                }
            } while (false);
        }

        Logger.i(TAG, "arguments:" + Arrays.toString(arguments));

        Class<?> returnType = originMethodInfo.returnType;
        Object artMethod = originMethodInfo.method;

        Logger.d(TAG, "leave bridge function");

        if (returnType == void.class) {
            onHookVoid(artMethod, receiver, arguments);
            return 0;
        } else if (returnType == char.class) {
            return onHookChar(artMethod, receiver, arguments);
        } else if (returnType == byte.class) {
            return onHookByte(artMethod, receiver, arguments);
        } else if (returnType == short.class) {
            return onHookShort(artMethod, receiver, arguments);
        } else if (returnType == int.class) {
            final int i = onHookInt(artMethod, receiver, arguments);
            Log.i(TAG, "leave bridge :" + i);
            return i;
        } else if (returnType == long.class) {
            return onHookLong(artMethod, receiver, arguments);
        } else if (returnType == float.class) {
            return onHookFloat(artMethod, receiver, arguments);
        } else if (returnType == double.class) {
            return onHookDouble(artMethod, receiver, arguments);
        } else if (returnType == boolean.class) {
            return onHookBoolean(artMethod, receiver, arguments);
        } else {
            return onHookObject(artMethod, receiver, arguments);
        }
    }

    private static Object wrapArgument(Class<?> type, long self, long value) {
        return wrapArgument(type, self, ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(value).array());
    }

    private static Object wrapArgument(Class<?> type, long self, byte[] value) {
        final ByteBuffer byteBuffer = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN);
        if (type.isPrimitive()) {
            if (type == int.class) {
                return byteBuffer.getInt();
            } else if (type == long.class) {
                return byteBuffer.getLong();
            } else if (type == float.class) {
                return byteBuffer.getFloat();
            } else if (type == short.class) {
                return byteBuffer.getShort();
            } else if (type == byte.class) {
                return byteBuffer.get();
            } else if (type == char.class) {
                return byteBuffer.getChar();
            } else if (type == double.class) {
                return byteBuffer.getDouble();
            } else if (type == boolean.class) {
                return byteBuffer.getInt() == 0;
            } else {
                throw new RuntimeException("unknown type:" + type);
            }
        } else {
            long address = byteBuffer.getLong();
            Object object = EpicNative.getObject(self, address);
            Logger.i(TAG, "wrapArgument, address: 0x" + Long.toHexString(address) + ", value:" + object);
            return object;
        }
    }

    private static Map<Class<?>, String> bridgeMethodMap = new HashMap<>();
    static {
        Class<?>[] primitiveTypes = new Class[]{boolean.class, byte.class, char.class, short.class,
                int.class, long.class, float.class, double.class};
        for (Class<?> primitiveType : primitiveTypes) {
            bridgeMethodMap.put(primitiveType, primitiveType.getName() + "Bridge");
        }
        bridgeMethodMap.put(void.class, "voidBridge");
        bridgeMethodMap.put(Object.class, "referenceBridge");
    }

    public static Method getBridgeMethod(Class<?> returnType) {
        try {
            final String bridgeMethod = bridgeMethodMap.get(returnType.isPrimitive() ? returnType : Object.class);
            Logger.d(TAG, "bridge method:" + bridgeMethod + ", map:" + bridgeMethodMap);
            Method method = Entry64.class.getDeclaredMethod(bridgeMethod, long.class, long.class,
                    long.class, long.class, long.class, long.class, long.class);
            method.setAccessible(true);
            return method;
        } catch (Throwable e) {
            throw new RuntimeException("error", e);
        }
    }
}