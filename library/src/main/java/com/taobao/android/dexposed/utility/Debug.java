/*
 * Original work Copyright (c) 2014-2015, Marvin Wißfeld
 * Modified work Copyright (c) 2016, Alibaba Mobile Infrastructure (Android) Team
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

package com.taobao.android.dexposed.utility;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;

import me.weishu.epic.art.EpicNative;
import me.weishu.epic.art.method.ArtMethod;

public final class Debug {
    private static final String TAG = "Dexposed";
    private static final long HEXDUMP_BYTES_PER_LINE = 16;

    public static final boolean DEBUG = true;
    public static final boolean WARN = true;

    private Debug() {
    }

    public static String addrHex(long i) {
        if (Runtime.is64Bit()) {
            return longHex(i);
        } else {
            return intHex((int) i);
        }
    }

    public static String longHex(long i) {
        return String.format("0x%016X", i);
    }

    public static String intHex(int i) {
        return String.format("0x%08X", i);
    }

    public static String byteHex(byte b) {
        return String.format("%02X", b);
    }

    public static String dump(byte[] b, long start) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            if (i % 8 == 0) {
                sb.append(addrHex(start + i)).append(":");
            }
            sb.append(byteHex(b[i])).append(" ");
            if (i % 8 == 7) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
    public static String hexdump(byte[] bytes, long start) {
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0 - (int) (start % HEXDUMP_BYTES_PER_LINE); i < bytes.length; i++) {
//            long num = Math.abs((start + i) % HEXDUMP_BYTES_PER_LINE);
//            if (num == 0 && sb.length() > 0)
//                sb.append('\n');
//            if (num == 0)
//                sb.append(addrHex(start + i)).append(": ");
//            if (num == 8)
//                sb.append(" ");
//            if (i >= 0)
//                sb.append(Debug.byteHex(bytes[i])).append(" ");
//            else
//                sb.append("   ");
//        }
//        return sb.toString().trim();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            if (i % 8 == 0) {
                sb.append(addrHex(start + i)).append(":");
            }
            sb.append(byteHex(bytes[i])).append(" ");
            if (i % 8 == 7) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public static String methodDescription(Method method) {
        return method.getDeclaringClass().getName() + "->" + method.getName() + " @" +
                addrHex(ArtMethod.of(method).getEntryPointFromQuickCompiledCode()) +
                " +" + addrHex(ArtMethod.of(method).getAddress());
    }

    public static void dumpMaps() {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/self/maps"));
            String line;
            while ((line = br.readLine()) != null) {
                Log.i(TAG, line);
            }
        } catch (IOException e) {
            Log.e(TAG, "dumpMaps error");
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    // ignore.
                }
            }
        }
    }

    public static void testGetObject() {
        Object o = new Object();
        long addr = Unsafe.getObjectAddress(o);
        Log.i(TAG, "object1:" + o);
        Log.i(TAG, "object address:" + Long.toHexString(addr));
        Log.i(TAG, "object2" + EpicNative.getObject(addr));
    }
}
