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

package me.weishu.epic.art;

import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.android.dexposed.utility.Debug;
import com.taobao.android.dexposed.utility.Logger;

import java.lang.reflect.Member;

import static com.taobao.android.dexposed.utility.Debug.DEBUG;
import static com.taobao.android.dexposed.utility.Debug.addrHex;


public final class EpicNative {

    static {
        System.loadLibrary("epic");
    }

    public static native long mmap(int length);

    public static native boolean munmap(long address, int length);

    public static native void memcpy(long src, long dest, int length);

    public static native void memput(byte[] bytes, long dest);

    public static native byte[] memget(long src, int length);

    public static native boolean munprotect(long addr, long len);

    public static native long getMethodAddress(Member method);

    public static native boolean cacheflush(long addr, long len);

    public static native long malloc(int sizeOfPtr);

    public static native Object getObject(long self, long address);

    public static native boolean compileMethod(Member method, long self);

    /**
     * suspend all running thread momently
     * @return a handle to resume all thread, used by {@link #resumeAll(long)}
     */
    public static native long suspendAll();

    /**
     * resume all thread which are suspend by {@link #suspendAll()}
     * only work abobe Android N
     * @param cookie the cookie return by {@link #suspendAll()}
     */
    public static native void resumeAll(long cookie);

    private static final String TAG = "EpicNative";

    private EpicNative() {
    }

    public static boolean compileMethod(Member method) {
        final long nativePeer = XposedHelpers.getLongField(Thread.currentThread(), "nativePeer");
        return compileMethod(method, nativePeer);
    }

    public static Object getObject(long address) {
        final long nativePeer = XposedHelpers.getLongField(Thread.currentThread(), "nativePeer");
        return getObject(nativePeer, address);
    }

    public static long map(int length) {
        long m = mmap(length);
        Logger.i(TAG, "Mapped memory of size " + length + " at " + addrHex(m));
        return m;
    }

    public static boolean unmap(long address, int length) {
        Logger.d(TAG, "Removing mapped memory of size " + length + " at " + addrHex(address));
        return munmap(address, length);
    }

    public static void put(byte[] bytes, long dest) {
        if (Debug.DEBUG) {
            Logger.d(TAG, "Writing memory to: " + addrHex(dest));
            Logger.d(TAG, Debug.hexdump(bytes, dest));
        }
        memput(bytes, dest);
    }

    public static byte[] get(long src, int length) {
        if (DEBUG) {
            Logger.d(TAG, "Reading " + length + " bytes from: " + addrHex(src));
        }
        byte[] bytes = memget(src, length);
        if (DEBUG) {
            Logger.d(TAG, Debug.hexdump(bytes, src));
        }
        return bytes;
    }

    public static boolean unprotect(long addr, long len) {
        Logger.d(TAG, "Disabling mprotect from " + addrHex(addr));
        return munprotect(addr, len);
    }

    public static void copy(long src, long dst, int length) {
        Logger.d(TAG, "Copy " + length + " bytes form " + addrHex(src) + " to " + addrHex(dst));
        memcpy(src, dst, length);
    }
}



