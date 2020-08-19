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

import android.util.Log;

import com.taobao.android.dexposed.DeviceCheck;
import com.taobao.android.dexposed.utility.Debug;
import com.taobao.android.dexposed.utility.Logger;
import com.taobao.android.dexposed.utility.Unsafe;

import java.lang.reflect.Member;

import de.robv.android.xposed.XposedHelpers;

import static com.taobao.android.dexposed.utility.Debug.addrHex;


public final class EpicNative {

    private static final String TAG = "EpicNative";
    private static volatile boolean useUnsafe = false;
    static {
        try {
            System.loadLibrary("epic");
            useUnsafe = DeviceCheck.isYunOS() || !isGetObjectAvailable();
            Log.i(TAG, "use unsafe ? " + useUnsafe);
        } catch (Throwable e) {
            Log.e(TAG, "init EpicNative error", e);
        }
    }

    public static native long mmap(int length);

    public static native boolean munmap(long address, int length);

    public static native void memcpy(long src, long dest, int length);

    public static native void memput(byte[] bytes, long dest);

    public static native byte[] memget(long src, int length);

    public static native boolean munprotect(long addr, long len);

    public static native long getMethodAddress(Member method);

    public static native void MakeInitializedClassVisibilyInitialized(long self);

    public static native boolean cacheflush(long addr, long len);

    public static native long malloc(int sizeOfPtr);

    public static native Object getObjectNative(long self, long address);

    private static native boolean isGetObjectAvailable();

    public static Object getObject(long self, long address) {
        if (useUnsafe) {
            return Unsafe.getObject(address);
        } else {
            return getObjectNative(self, address);
        }
    }

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

    /**
     * stop jit compiler in runtime.
     * Warning: Just for experiment Do not call this now!!!
     * @return cookie use by {@link #startJit(long)}
     */
    public static native long stopJit();

    /**
     * start jit compiler stop by {@link #stopJit()}
     * Warning: Just for experiment Do not call this now!!!
     * @param cookie the cookie return by {@link #stopJit()}
     */
    public static native void startJit(long cookie);

    // FIXME: 17/12/29 reimplement it with pure native code.
    static native boolean activateNative(long jumpToAddress, long pc, long sizeOfTargetJump, long sizeOfBridgeJump, byte[] code);

    /**
     * Disable the moving gc of runtime.
     * Warning: Just for experiment Do not call this now!!!
     * @param api the api level
     */
    public static native void disableMovingGc(int api);


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

    public static void MakeInitializedClassVisibilyInitialized() {
        final long nativePeer = XposedHelpers.getLongField(Thread.currentThread(), "nativePeer");
        MakeInitializedClassVisibilyInitialized(nativePeer);
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
        Logger.d(TAG, "Reading " + length + " bytes from: " + addrHex(src));
        byte[] bytes = memget(src, length);
        Logger.d(TAG, Debug.hexdump(bytes, src));
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



