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

import com.taobao.android.dexposed.utility.Debug;
import com.taobao.android.dexposed.utility.Logger;
import com.taobao.android.dexposed.utility.Runtime;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import me.weishu.epic.art.EpicNative;

/**
 * The Offset of field in an ArtMethod
 */
class Offset {

    private static final String TAG = "Offset";

    /**
     * the offset of the entry point
     */
    static Offset ART_QUICK_CODE_OFFSET;

    /**
     * the offset of the access flag
     */
    static Offset ART_ACCESS_FLAG_OFFSET;

    /**
     * the offset of a jni entry point
     */
    static Offset ART_JNI_ENTRY_OFFSET;

    static {
        initFields();
    }

    private enum BitWidth {
        DWORD(4),
        QWORD(8);

        BitWidth(int width) {
            this.width = width;
        }

        int width;
    }

    private long offset;
    private BitWidth length;

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public BitWidth getLength() {
        return length;
    }

    public void setLength(BitWidth length) {
        this.length = length;
    }

    public static long read(long base, Offset offset) {
        long address = base + offset.offset;
        byte[] bytes = EpicNative.get(address, offset.length.width);
        if (offset.length == BitWidth.DWORD) {
            return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt() & 0xFFFFFFFFL;
        } else {
            return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getLong();
        }
    }

    public static void write(long base, Offset offset, long value) {
        long address = base + offset.offset;
        byte[] bytes;
        if (offset.length == BitWidth.DWORD) {
            if (value > 0xFFFFFFFFL) {
                throw new IllegalStateException("overflow may occur");
            } else {
                bytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt((int) value).array();
            }
        } else {
            bytes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(value).array();
        }
        EpicNative.put(bytes, address);
    }

    private static void initFields() {
        ART_QUICK_CODE_OFFSET = new Offset();
        ART_ACCESS_FLAG_OFFSET = new Offset();
        ART_JNI_ENTRY_OFFSET = new Offset();

        ART_ACCESS_FLAG_OFFSET.setLength(Offset.BitWidth.DWORD);

        final int apiLevel = Build.VERSION.SDK_INT;

        if (Runtime.is64Bit()) {
            ART_QUICK_CODE_OFFSET.setLength(Offset.BitWidth.QWORD);
            ART_JNI_ENTRY_OFFSET.setLength(BitWidth.QWORD);
            switch (apiLevel) {
                case Build.VERSION_CODES.R:
                case Build.VERSION_CODES.Q:
                case Build.VERSION_CODES.P:
                    ART_QUICK_CODE_OFFSET.setOffset(32);
                    ART_JNI_ENTRY_OFFSET.setOffset(24);
                    ART_ACCESS_FLAG_OFFSET.setOffset(4);
                    break;
                case Build.VERSION_CODES.O_MR1:
                case Build.VERSION_CODES.O:
                    ART_QUICK_CODE_OFFSET.setOffset(40);
                    ART_JNI_ENTRY_OFFSET.setOffset(32);
                    ART_ACCESS_FLAG_OFFSET.setOffset(4);
                    break;
                case Build.VERSION_CODES.N_MR1:
                case Build.VERSION_CODES.N:
                    ART_QUICK_CODE_OFFSET.setOffset(48);
                    ART_JNI_ENTRY_OFFSET.setOffset(40);
                    ART_ACCESS_FLAG_OFFSET.setOffset(4);
                    break;
                case Build.VERSION_CODES.M:
                    ART_QUICK_CODE_OFFSET.setOffset(48);
                    ART_JNI_ENTRY_OFFSET.setOffset(40);
                    ART_ACCESS_FLAG_OFFSET.setOffset(12);
                    break;
                case Build.VERSION_CODES.LOLLIPOP_MR1:
                    ART_QUICK_CODE_OFFSET.setOffset(52);
                    ART_JNI_ENTRY_OFFSET.setOffset(44);
                    ART_ACCESS_FLAG_OFFSET.setOffset(20);
                    break;
                case Build.VERSION_CODES.LOLLIPOP:
                    ART_QUICK_CODE_OFFSET.setOffset(40);
                    ART_QUICK_CODE_OFFSET.setLength(BitWidth.QWORD);
                    ART_JNI_ENTRY_OFFSET.setOffset(32);
                    ART_JNI_ENTRY_OFFSET.setLength(BitWidth.QWORD);
                    ART_ACCESS_FLAG_OFFSET.setOffset(56);
                    break;
                case Build.VERSION_CODES.KITKAT:
                    ART_QUICK_CODE_OFFSET.setOffset(32);
                    ART_ACCESS_FLAG_OFFSET.setOffset(28);
                    break;
                default:
                    throw new RuntimeException("API LEVEL: " + apiLevel + " is not supported now : (");
            }
        } else {
            ART_QUICK_CODE_OFFSET.setLength(Offset.BitWidth.DWORD);
            ART_JNI_ENTRY_OFFSET.setLength(BitWidth.DWORD);
            switch (apiLevel) {
                case Build.VERSION_CODES.Q:
                case Build.VERSION_CODES.P:
                    ART_QUICK_CODE_OFFSET.setOffset(24);
                    ART_JNI_ENTRY_OFFSET.setOffset(20);
                    ART_ACCESS_FLAG_OFFSET.setOffset(4);
                    break;
                case Build.VERSION_CODES.O_MR1:
                case Build.VERSION_CODES.O:
                    ART_QUICK_CODE_OFFSET.setOffset(28);
                    ART_JNI_ENTRY_OFFSET.setOffset(24);
                    ART_ACCESS_FLAG_OFFSET.setOffset(4);
                    break;
                case Build.VERSION_CODES.N_MR1:
                case Build.VERSION_CODES.N:
                    ART_QUICK_CODE_OFFSET.setOffset(32);
                    ART_JNI_ENTRY_OFFSET.setOffset(28);
                    ART_ACCESS_FLAG_OFFSET.setOffset(4);
                    break;
                case Build.VERSION_CODES.M:
                    ART_QUICK_CODE_OFFSET.setOffset(36);
                    ART_JNI_ENTRY_OFFSET.setOffset(32);
                    ART_ACCESS_FLAG_OFFSET.setOffset(12);
                    break;
                case Build.VERSION_CODES.LOLLIPOP_MR1:
                    ART_QUICK_CODE_OFFSET.setOffset(44);
                    ART_JNI_ENTRY_OFFSET.setOffset(40);
                    ART_ACCESS_FLAG_OFFSET.setOffset(20);
                    break;
                case Build.VERSION_CODES.LOLLIPOP:
                    ART_QUICK_CODE_OFFSET.setOffset(40);
                    ART_QUICK_CODE_OFFSET.setLength(BitWidth.QWORD);
                    ART_JNI_ENTRY_OFFSET.setOffset(32);
                    ART_JNI_ENTRY_OFFSET.setLength(BitWidth.QWORD);
                    ART_ACCESS_FLAG_OFFSET.setOffset(56);
                    break;
                case Build.VERSION_CODES.KITKAT:
                    ART_QUICK_CODE_OFFSET.setOffset(32);
                    ART_ACCESS_FLAG_OFFSET.setOffset(28);
                    break;
                default:
                    throw new RuntimeException("API LEVEL: " + apiLevel + " is not supported now : (");
            }
        }
        if (Debug.DEBUG) {
            Logger.i(TAG, "quick code offset: " + ART_QUICK_CODE_OFFSET.getOffset());
            Logger.i(TAG, "access flag offset: " + ART_ACCESS_FLAG_OFFSET.getOffset());
            Logger.i(TAG, "jni code offset: " + ART_JNI_ENTRY_OFFSET.getOffset());

        }
    }
}
