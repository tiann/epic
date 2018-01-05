/*
 * Copyright 2014-2015 Marvin Wißfeld
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.taobao.android.dexposed.utility;

import android.util.Log;

import java.lang.reflect.Field;

public final class Unsafe {
    private static final String TAG = "Unsafe";

    private static Object unsafe;
    private static Class unsafeClass;

    static {
        try {
            unsafeClass = Class.forName("sun.misc.Unsafe");
            Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            unsafe = theUnsafe.get(null);
        } catch (Exception e) {
            try {
                final Field theUnsafe = unsafeClass.getDeclaredField("THE_ONE");
                theUnsafe.setAccessible(true);
                unsafe = theUnsafe.get(null);
            } catch (Exception e2) {
                Log.w(TAG, "Unsafe not found o.O");
            }
        }
    }

    private Unsafe() {
    }

    @SuppressWarnings("unchecked")
    public static int arrayBaseOffset(Class cls) {
        try {
            return (int) unsafeClass.getDeclaredMethod("arrayBaseOffset", Class.class).invoke(unsafe, cls);
        } catch (Exception e) {
            Log.w(TAG, e);
            return 0;
        }
    }

    @SuppressWarnings("unchecked")
    public static int arrayIndexScale(Class cls) {
        try {
            return (int) unsafeClass.getDeclaredMethod("arrayIndexScale", Class.class).invoke(unsafe, cls);
        } catch (Exception e) {
            Log.w(TAG, e);
            return 0;
        }
    }

    @SuppressWarnings("unchecked")
    public static long objectFieldOffset(Field field) {
        try {
            return (long) unsafeClass.getDeclaredMethod("objectFieldOffset", Field.class).invoke(unsafe, field);
        } catch (Exception e) {
            Log.w(TAG, e);
            return 0;
        }
    }

    @SuppressWarnings("unchecked")
    public static int getInt(Object array, long offset) {
        try {
            return (int) unsafeClass.getDeclaredMethod("getInt", Object.class, long.class).invoke(unsafe, array, offset);
        } catch (Exception e) {
            Log.w(TAG, e);
            return 0;
        }
    }

    @SuppressWarnings("unchecked")
    public static long getLong(Object array, long offset) {
        try {
            return (long) unsafeClass.getDeclaredMethod("getLong", Object.class, long.class).invoke(unsafe, array, offset);
        } catch (Exception e) {
            Log.w(TAG, e);
            return 0;
        }
    }

    @SuppressWarnings("unchecked")
    public static void putLong(Object array, long offset, long value) {
        try {
            unsafeClass.getDeclaredMethod("putLongVolatile", Object.class, long.class, long.class).invoke(unsafe, array, offset, value);
        } catch (Exception e) {
            try {
                unsafeClass.getDeclaredMethod("putLong", Object.class, long.class, long.class).invoke(unsafe, array, offset, value);
            } catch (Exception e1) {
                Log.w(TAG, e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void putInt(Object array, long offset, int value) {
        try {
            unsafeClass.getDeclaredMethod("putIntVolatile", Object.class, long.class, int.class).invoke(unsafe, array, offset, value);
        } catch (Exception e) {
            try {
                unsafeClass.getDeclaredMethod("putIntVolatile", Object.class, long.class, int.class).invoke(unsafe, array, offset, value);
            } catch (Exception e1) {
                Log.w(TAG, e);
            }
        }
    }

    public static long getObjectAddress(Object obj) {
        try {
            Object[] array = new Object[]{obj};
            if (arrayIndexScale(Object[].class) == 8) {
                return getLong(array, arrayBaseOffset(Object[].class));
            } else {
                return 0xffffffffL & getInt(array, arrayBaseOffset(Object[].class));
            }
        } catch (Exception e) {
            Log.w(TAG, e);
            return -1;
        }
    }

    /**
     * get Object from address, refer: http://mishadoff.com/blog/java-magic-part-4-sun-dot-misc-dot-unsafe/
     * @param address the address of a object.
     * @return
     */
    public static Object getObject(long address) {
        Object[] array = new Object[]{null};
        long baseOffset = arrayBaseOffset(Object[].class);
        if (Runtime.is64Bit()) {
            putLong(array, baseOffset, address);
        } else {
            putInt(array, baseOffset, (int) address);
        }
        return array[0];
    }
}
