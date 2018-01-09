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

import android.os.Build;
import android.util.Pair;

import com.taobao.android.dexposed.DexposedBridge;
import com.taobao.android.dexposed.utility.Debug;
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
public class Entry {

    private final static String TAG = "Entry";

    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

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
    private static void voidBridge(int r1, int self, int struct) {
        referenceBridge(r1, self, struct);
    }

    private static boolean booleanBridge(int r1, int self, int struct) {
        return (Boolean) referenceBridge(r1, self, struct);
    }

    private static byte byteBridge(int r1, int self, int struct) {
        return (Byte) referenceBridge(r1, self, struct);
    }

    private static short shortBridge(int r1, int self, int struct) {
        return (Short) referenceBridge(r1, self, struct);
    }

    private static char charBridge(int r1, int self, int struct) {
        return (Character) referenceBridge(r1, self, struct);
    }

    private static int intBridge(int r1, int self, int struct) {
        return (Integer) referenceBridge(r1, self, struct);
    }

    private static long longBridge(int r1, int self, int struct) {
        return (Long) referenceBridge(r1, self, struct);
    }

    private static float floatBridge(int r1, int self, int struct) {
        return (Float) referenceBridge(r1, self, struct);
    }

    private static double doubleBridge(int r1, int self, int struct) {
        return (Double) referenceBridge(r1, self, struct);
    }
    //endregion

    private static Object referenceBridge(int r1, int self, int struct) {
        Logger.i(TAG, "enter bridge function.");

        // struct {
        //     void* sp;
        //     void* r2;
        //     void* r3;
        //     void* sourceMethod
        // }
        // sp + 16 = r4

        Logger.i(TAG, "struct:" + Long.toHexString(struct));

        final int sp = ByteBuffer.wrap(EpicNative.get(struct, 4)).order(ByteOrder.LITTLE_ENDIAN).getInt();

        // Logger.i(TAG, "stack:" + Debug.hexdump(EpicNative.get(sp, 96), 0));

        final byte[] rr1 = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(r1).array();
        final byte[] r2 = EpicNative.get(struct + 4, 4);

        final byte[] r3 = EpicNative.get(struct + 8, 4);

        Logger.d(TAG, "r1:" + Debug.hexdump(rr1, 0));
        Logger.d(TAG, "r2:" + Debug.hexdump(r2, 0));
        Logger.d(TAG, "r3:" + Debug.hexdump(r3, 0));

        final int sourceMethod = ByteBuffer.wrap(EpicNative.get(struct + 12, 4)).order(ByteOrder.LITTLE_ENDIAN).getInt();
        Logger.i(TAG, "sourceMethod:" + Integer.toHexString(sourceMethod));

        Epic.MethodInfo originMethodInfo = Epic.getMethodInfo(sourceMethod);
        Logger.i(TAG, "originMethodInfo :" + originMethodInfo);

        final Pair<Object, Object[]> constructArguments = constructArguments(originMethodInfo, self, rr1, r2, r3, sp);
        Object receiver = constructArguments.first;
        Object[] arguments = constructArguments.second;

        Logger.i(TAG, "arguments:" + Arrays.toString(arguments));

        Class<?> returnType = originMethodInfo.returnType;
        Object artMethod = originMethodInfo.method;

        Logger.i(TAG, "leave bridge function");

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
            return onHookInt(artMethod, receiver, arguments);
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

    /**
     * construct the method arguments from register r1, r2, r3 and stack
     * @param r1 register r1 value
     * @param r2 register r2 value
     * @param r3 register r3 value
     * @param sp stack pointer
     * @return arguments passed to the callee method
     */
    private static Pair<Object, Object[]> constructArguments(Epic.MethodInfo originMethodInfo, int self,
                                                             byte[] r1, byte[] r2, byte[] r3, int sp) {
        boolean isStatic = originMethodInfo.isStatic;

        int numberOfArgs;
        Class<?>[] typeOfArgs;
        if (isStatic) {

            // static argument, r1, r2, r3, sp + 16

            // sp + 0 = ArtMethod (ourself)
            // sp + 4 = r1 (may be earased)
            // sp + 8 = r2 (may be earased)
            // sp + 12 = r3 (may be earased)
            // sp + 16 = r4, remain

            numberOfArgs = originMethodInfo.paramNumber;
            typeOfArgs = originMethodInfo.paramTypes;
        } else {
            // non-static, r1 = receiver; r2, r3, sp + 16 is arguments.

            // sp + 0 = ArtMethod (ourself)
            // sp + 4 = r1 = this (may be earased)
            // sp + 8 = r2 = first argument (may be earased)
            // sp + 12 = r3 = second argument (may be earased)
            // sp + 16 = third argument, remain
            numberOfArgs = 1 + originMethodInfo.paramNumber;
            typeOfArgs = new Class<?>[numberOfArgs];
            typeOfArgs[0] = Object.class; // this
            System.arraycopy(originMethodInfo.paramTypes, 0, typeOfArgs, 1, originMethodInfo.paramTypes.length);
        }

        Object[] arguments = new Object[numberOfArgs];

        int currentStackPosition = 4; // sp + 0 = ArtMethod, sp + 4... start store arguments.
        final int argumentStackBegin = 16; // sp + 4 = r1, sp + 8 = r2, sp + 12 = r3, sp + 16 start in stack.

        int[] argStartPos = new int[numberOfArgs];

        for (int i = 0; i < numberOfArgs; i++) {
            Class<?> typeOfArg = typeOfArgs[i];
            int typeLength = getTypeLength(typeOfArg);
            argStartPos[i] = currentStackPosition;
            currentStackPosition += typeLength;
        }

        int argTotalLength = currentStackPosition;
        byte[] argBytes = new byte[argTotalLength];

        do {
            if (argTotalLength <= 4) break;

            boolean align = Build.VERSION.SDK_INT >= 23 && numberOfArgs > 0 && getTypeLength(typeOfArgs[0]) == 8;
            if (align) {
                System.arraycopy(r2, 0, argBytes, 4, 4);
                System.arraycopy(r3, 0, argBytes, 8, 4);
                if (argTotalLength <= 12) break;
                System.arraycopy(EpicNative.get(sp + 12, 4), 0, argBytes, 12, 4);
            } else {
                System.arraycopy(r1, 0, argBytes, 4, 4);

                if (argTotalLength <= 8) break;
                System.arraycopy(r2, 0, argBytes, 8, 4);
                if (argTotalLength <= 12) break;
                System.arraycopy(r3, 0, argBytes, 12, 4);
            }

            if (argTotalLength <= 16) break;

            byte[] argInStack = EpicNative.get(sp + 16, argTotalLength - 16);
            System.arraycopy(argInStack, 0, argBytes, 16, argTotalLength - 16);
        } while (false);

        //region ---------------Process Arguments passing in Android M---------------
        if (Build.VERSION.SDK_INT == 23) {
            // Android M, fix sp + 12
            if (argTotalLength <= 12) {
                // Nothing
            } else {
                if (argTotalLength <= 16) {
                    if (getTypeLength(typeOfArgs[0]) == 8) {
                        // first is 8byte
                        System.arraycopy(EpicNative.get(sp + 44, 4), 0, argBytes, 12, 4);
                    } else {
                        // 48, 444: normal.
                    }
                } else {
                    boolean isR3Grabbed = true;
                    if (numberOfArgs >= 2) {
                        int arg1TypeLength = getTypeLength(typeOfArgs[0]);
                        int arg2TypeLength = getTypeLength(typeOfArgs[1]);
                        if (arg1TypeLength == 4 && arg2TypeLength == 8) {
                            isR3Grabbed = false;
                        }

                        if (numberOfArgs == 2 && arg1TypeLength == 8 && arg2TypeLength == 8) {
                            // in this case, we have no reference register to local r3, just hard code now :(
                            System.arraycopy(EpicNative.get(sp + 44, 4), 0, argBytes, 12, 4);
                            isR3Grabbed = false;
                        }
                    }
                    if (numberOfArgs >= 3) {
                        int arg1TypeLength = getTypeLength(typeOfArgs[0]);
                        int arg2TypeLength = getTypeLength(typeOfArgs[1]);
                        int arg3TypeLength = getTypeLength(typeOfArgs[2]);
                        if (arg1TypeLength == 4 && arg2TypeLength == 4 && arg3TypeLength == 4) {
                            // in this case: r1 = arg1; r2 = arg2; r3 = arg3, normal.
                            isR3Grabbed = false;
                        }
                        if (numberOfArgs == 3 && arg1TypeLength == 8 && arg2TypeLength == 4 && arg3TypeLength == 8) {
                            // strange case :)
                            System.arraycopy(EpicNative.get(sp + 52, 4), 0, argBytes, 12, 4);
                            isR3Grabbed = false;
                        }
                    }
                    if (isR3Grabbed) {
                        byte[] otherStoreInStack = Arrays.copyOfRange(argBytes, argumentStackBegin, argBytes.length);
                        int otherStoreInStackLength = otherStoreInStack.length;
                        int searchRegion = 0;
                        for (int i = argumentStackBegin + otherStoreInStackLength; ; i = i + 4) {
                            final byte[] bytes = EpicNative.get(sp + i, otherStoreInStackLength);
                            searchRegion += otherStoreInStackLength;
                            if (Arrays.equals(bytes, otherStoreInStack)) {
                                int originR3Index = sp + i - 4;
                                final byte[] originR3 = EpicNative.get(originR3Index, 4);
                                Logger.d(TAG, "found other arguments in stack, index:" + i + ", origin r3:" + Arrays.toString(originR3));
                                System.arraycopy(originR3, 0, argBytes, 12, 4);
                                break;
                            }
                            if (searchRegion > (1 << 10)) {
                                throw new RuntimeException("can not found the modify r3 register!!!");
                            }
                        }
                    }
                }
            }
        }
        //endregion

        Logger.d(TAG, "argBytes: " + Debug.hexdump(argBytes, 0));

        for (int i = 0; i < numberOfArgs; i++) {
            final Class<?> typeOfArg = typeOfArgs[i];
            final int startPos = argStartPos[i];
            final int typeLength = getTypeLength(typeOfArg);
            byte[] argWithBytes = Arrays.copyOfRange(argBytes, startPos, startPos + typeLength);
            arguments[i] = wrapArgument(typeOfArg, self, argWithBytes);
//            Logger.d(TAG, "argument[" + i + "], startPos:" + startPos + ", typeOfLength:" + typeLength);
//            Logger.d(TAG, "argWithBytes:" + Debug.hexdump(argWithBytes, 0) + ", value:" + arguments[i]);
        }

        Object thiz = null;
        Object[] parameters = EMPTY_OBJECT_ARRAY;
        if (isStatic) {
            parameters = arguments;
        } else {
            thiz = arguments[0];
            int argumentLength = arguments.length;
            if (argumentLength > 1) {
                parameters = Arrays.copyOfRange(arguments, 1, argumentLength);
            }
        }

        return Pair.create(thiz, parameters);
    }

    private static Object wrapArgument(Class<?> type, int self, byte[] value) {
        final ByteBuffer byteBuffer = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN);
        Logger.d(TAG, "wrapArgument: type:" + type);
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
            int address = byteBuffer.getInt();
            Object object = EpicNative.getObject(self, address);
            // Logger.i(TAG, "wrapArgument, address: 0x" + Long.toHexString(address) + ", value:" + object);
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
        bridgeMethodMap.put(Object.class, "referenceBridge");
        bridgeMethodMap.put(void.class, "voidBridge");
    }

    public static Method getBridgeMethod(Class<?> returnType) {
        try {
            final String bridgeMethod = bridgeMethodMap.get(returnType.isPrimitive() ? returnType : Object.class);
            Logger.i(TAG, "bridge method:" + bridgeMethod + ", map:" + bridgeMethodMap);
            Method method = Entry.class.getDeclaredMethod(bridgeMethod, int.class, int.class, int.class);
            method.setAccessible(true);
            return method;
        } catch (Throwable e) {
            throw new RuntimeException("error", e);
        }
    }

    private static int getTypeLength(Class<?> clazz) {
        if (clazz == long.class || clazz == double.class) {
            return 8; // double & long are 8 bytes.
        } else {
            return 4;
        }
    }
}
