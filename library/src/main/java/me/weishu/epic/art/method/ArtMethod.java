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

package me.weishu.epic.art.method;

import android.os.Build;
import android.util.Log;

import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.android.dexposed.utility.Logger;
import com.taobao.android.dexposed.utility.NeverCalled;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import me.weishu.epic.art.EpicNative;

/**
 * Object stands for a Java Method, may be a constructor or a method.
 */
public class ArtMethod {

    private static final String TAG = "ArtMethod";

    /**
     * The address of the Java method. this is not the real memory address of the java.lang.reflect.Method
     * But the address used by VM which stand for the Java method.
     * generally, it was the address of art::mirror::ArtMethod.
     */
    private long address;

    /**
     * the origin object if this is a constructor
     */
    private Constructor constructor;

    /**
     * the origin object if this is a method;
     */
    private Method method;

    /**
     * The size of ArtMethod, usually the java part of ArtMethod may not stand for the whole one
     * may be some native field is placed in the end of header.
     */
    private static int artMethodSize = -1;

    private ArtMethod(Constructor constructor) {
        if (constructor == null) {
            throw new IllegalArgumentException("constructor can not be null");
        }
        this.constructor = constructor;
        init();
    }

    private ArtMethod(Method method) {
        if (method == null) {
            throw new IllegalArgumentException("method can not be null");
        }
        this.method = method;
        init();
    }

    private void init() {
        if (constructor != null) {
            address = EpicNative.getMethodAddress(constructor);
        } else {
            address = EpicNative.getMethodAddress(method);
        }
    }

    public static ArtMethod of(Method method) {
        return new ArtMethod(method);
    }

    public static ArtMethod of(Constructor constructor) {
        return new ArtMethod(constructor);
    }


    public ArtMethod backup() {
        try {
            Class<?> abstractMethodClass = Class.forName("java.lang.reflect.AbstractMethod");

            Object executable = this.getExecutable();
            ArtMethod artMethod;
            if (Build.VERSION.SDK_INT < 23) {
                Class<?> artMethodClass = Class.forName("java.lang.reflect.ArtMethod");
                //Get the original artMethod field
                Field artMethodField = abstractMethodClass.getDeclaredField("artMethod");
                if (!artMethodField.isAccessible()) {
                    artMethodField.setAccessible(true);
                }
                Object srcArtMethod = artMethodField.get(executable);

                Constructor<?> constructor = artMethodClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                Object destArtMethod = constructor.newInstance();

                //Fill the fields to the new method we created
                for (Field field : artMethodClass.getDeclaredFields()) {
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    field.set(destArtMethod, field.get(srcArtMethod));
                }
                Method newMethod = Method.class.getConstructor(artMethodClass).newInstance(destArtMethod);
                newMethod.setAccessible(true);
                artMethod = ArtMethod.of(newMethod);

                artMethod.setEntryPointFromQuickCompiledCode(getEntryPointFromQuickCompiledCode());
                artMethod.setEntryPointFromJni(getEntryPointFromJni());
            } else {
                Constructor<Method> constructor = Method.class.getDeclaredConstructor();
                // we can't use constructor.setAccessible(true); because Google does not like it
                // AccessibleObject.setAccessible(new AccessibleObject[]{constructor}, true);
                Field override = AccessibleObject.class.getDeclaredField(
                        Build.VERSION.SDK_INT == Build.VERSION_CODES.M ? "flag" : "override");
                override.setAccessible(true);
                override.set(constructor, true);

                Method m = constructor.newInstance();
                m.setAccessible(true);
                for (Field field : abstractMethodClass.getDeclaredFields()) {
                    field.setAccessible(true);
                    field.set(m, field.get(executable));
                }
                Field artMethodField = abstractMethodClass.getDeclaredField("artMethod");
                artMethodField.setAccessible(true);
                int artMethodSize = getArtMethodSize();
                long memoryAddress = EpicNative.map(artMethodSize);

                byte[] data = EpicNative.get(address, artMethodSize);
                EpicNative.put(data, memoryAddress);
                artMethodField.set(m, memoryAddress);
                artMethod = ArtMethod.of(m);
            }
            artMethod.makePrivate();
            artMethod.setAccessible(true);
            return artMethod;


        } catch (Throwable e) {
            Log.e(TAG, "backup method error:", e);
            throw new IllegalStateException("Cannot create backup method from :: " + getExecutable(), e);
        }
    }


    private static Map<Class<?>, Object> defaultValue = new HashMap<Class<?>, Object>() {{
        put(boolean.class, false);
        put(byte.class, 0);
        put(short.class, 0);
        put(int.class, 0x1);
        put(float.class, 0f);
        put(double.class, 0);
        put(long.class, 0);
        put(String.class, "");
        put(Object.class, new Object());
    }};

    /**
     * make the constructor or method accessible
     * @param accessible accessible
     */
    public void setAccessible(boolean accessible) {
        if (constructor != null) {
            constructor.setAccessible(accessible);
        } else {
            method.setAccessible(accessible);
        }
    }

    /**
     * get the origin method's name
     * @return constructor name of method name
     */
    public String getName() {
        if (constructor != null) {
            return constructor.getName();
        } else {
            return method.getName();
        }
    }

    /**
     * Force compile the method to avoid interpreter mode.
     * This is only used above Android N
     * @return if compile success return true, otherwise false.
     */
    public boolean compile() {
        if (constructor != null) {
            return EpicNative.compileMethod(constructor);
        } else {
            return EpicNative.compileMethod(method);
        }
    }

    /**
     * invoke the origin method
     * @param receiver the receiver
     * @param args origin method/constructor's parameters
     * @return origin method's return value.
     * @throws IllegalAccessException throw if no access, impossible.
     * @throws InvocationTargetException invoke target error.
     * @throws InstantiationException throw when the constructor can not create instance.
     */
    public Object invoke(Object receiver, Object... args) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        if (constructor != null) {
            return constructor.newInstance(args);
        } else {
            return method.invoke(receiver, args);
        }
    }

    /**
     * get the modifiers of origin method/constructor
     * @return the modifiers
     */
    public int getModifiers() {
        if (constructor != null) {
            return constructor.getModifiers();
        } else {
            return method.getModifiers();
        }
    }

    /**
     * get the parameter type of origin method/constructor
     * @return the parameter types.
     */
    public Class<?>[] getParameterTypes() {
        if (constructor != null) {
            return constructor.getParameterTypes();
        } else {
            return method.getParameterTypes();
        }
    }

    /**
     * get the return type of origin method/constructor
     * @return the return type, if it is a constructor, return Object.class
     */
    public Class<?> getReturnType() {
        if (constructor != null) {
            return Object.class;
        } else {
            return method.getReturnType();
        }
    }

    /**
     * @return the origin method/constructor
     */
    public Object getExecutable() {
        if (constructor != null) {
            return constructor;
        } else {
            return method;
        }
    }

    /**
     * get the memory address of the inner constructor/method
     * @return the method address, in general, it was the pointer of art::mirror::ArtMethod
     */
    public long getAddress() {
        return address;
    }

    /**
     * get the unique identifier of the constructor/method
     * @return the method identifier
     */
    public String getIdentifier() {
        // Can we use address, may gc move it??
        return String.valueOf(getAddress());
    }

    /**
     * force set the private flag of the method.
     */
    public void makePrivate() {
        int accessFlags = getAccessFlags();
        accessFlags &= ~Modifier.PUBLIC;
        accessFlags |= Modifier.PRIVATE;
        setAccessFlags(accessFlags);
    }

    /**
     * the static method is lazy resolved, when not resolved, the entry point is a trampoline of
     * a bridge, we can not hook these entry. this method force the static method to be resolved.
     */
    public void ensureResolved() {
        if (!Modifier.isStatic(getModifiers())) {
            Log.i(TAG, "not static, ignore.");
            return;
        }
        final Class<?>[] parameterTypes = getParameterTypes();
        int numberOfParams = parameterTypes.length;
        Object[] args = new Object[parameterTypes.length];
        for (int i = 0; i < numberOfParams; i++) {
            Class<?> type = parameterTypes[i];
            type = type.isPrimitive() ? type : Object.class;
            args[i] = defaultValue.get(type);
        }

        try {
            invoke(null, args);
            Log.i(TAG, "ensure resolved");
        } catch (Throwable e) {
            Log.e(TAG, "ensure resolved failed.");
        }
    }

    /**
     * The entry point of the quick compiled code.
     * @return the entry point.
     */
    public long getEntryPointFromQuickCompiledCode() {
        return Offset.read(address, Offset.ART_QUICK_CODE_OFFSET);
    }

    /**
     * @param pointer_entry_point_from_quick_compiled_code the entry point.
     */
    public void setEntryPointFromQuickCompiledCode(long pointer_entry_point_from_quick_compiled_code) {
        Offset.write(address, Offset.ART_QUICK_CODE_OFFSET, pointer_entry_point_from_quick_compiled_code);
    }

    /**
     * @return the access flags of the method/constructor, not only stand for the modifiers.
     */
    public int getAccessFlags() {
        return (int) Offset.read(address, Offset.ART_ACCESS_FLAG_OFFSET);
    }

    public void setAccessFlags(int newFlags) {
        Offset.write(address, Offset.ART_ACCESS_FLAG_OFFSET, newFlags);
    }

    public void setEntryPointFromJni(long entryPointFromJni) {
        Offset.write(address, Offset.ART_JNI_ENTRY_OFFSET, entryPointFromJni);
    }

    public long getEntryPointFromJni() {
        return Offset.read(address, Offset.ART_JNI_ENTRY_OFFSET);
    }

    /**
     * The size of an art::mirror::ArtMethod, we use two rule method to measure the size
     * @return the size
     */
    public static int getArtMethodSize() {
        if (artMethodSize > 0) {
            return artMethodSize;
        }
        final Method rule1 = XposedHelpers.findMethodExact(ArtMethod.class, "rule1");
        final Method rule2 = XposedHelpers.findMethodExact(ArtMethod.class, "rule2");
        final long rule2Address = EpicNative.getMethodAddress(rule2);
        final long rule1Address = EpicNative.getMethodAddress(rule1);
        final long size = Math.abs(rule2Address - rule1Address);
        artMethodSize = (int) size;
        Logger.d(TAG, "art Method size: " + size);
        return artMethodSize;
    }

    private void rule1() {
        Log.i(TAG, "do not inline me!!");
    }

    private void rule2() {
        Log.i(TAG, "do not inline me!!");
    }
    public static long getQuickToInterpreterBridge() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return -1L;
        }
        final Method fake = XposedHelpers.findMethodExact(NeverCalled.class, "fake", int.class);
        return ArtMethod.of(fake).getEntryPointFromQuickCompiledCode();
    }

    public long getFieldOffset() {
        // searchOffset(address, )
        return 0L;
    }

    /**
     * search Offset in memory
     * @param base base address
     * @param range search range
     * @param value search value
     * @return the first address of value if found
     */
    public static long searchOffset(long base, long range, int value) {
        final int align = 4;
        final long step = range / align;
        for (long i = 0; i < step; i++) {
            long offset = i * align;
            final byte[] bytes = EpicNative.memget(base + i * align, align);
            final int valueInOffset = ByteBuffer.allocate(4).put(bytes).getInt();
            if (valueInOffset == value) {
                return offset;
            }
        }
        return -1;
    }

    public static long searchOffset(long base, long range, long value) {
        final int align = 4;
        final long step = range / align;
        for (long i = 0; i < step; i++) {
            long offset = i * align;
            final byte[] bytes = EpicNative.memget(base + i * align, align);
            final long valueInOffset = ByteBuffer.allocate(8).put(bytes).getLong();
            if (valueInOffset == value) {
                return offset;
            }
        }
        return -1;
    }
}
