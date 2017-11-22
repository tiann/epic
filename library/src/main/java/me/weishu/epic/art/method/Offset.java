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

import com.taobao.android.dexposed.utility.Runtime;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import me.weishu.epic.art.EpicNative;

/**
 * The Offset of field in an ArtMethod
 */
class Offset {

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
            if (value > Integer.MAX_VALUE) {
                throw new IllegalStateException("overflow may occured");
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
            if (apiLevel >= 26) {
                throw new RuntimeException("Unsupported now.");
            } else if (apiLevel >= 24) {
                ART_QUICK_CODE_OFFSET.setOffset(48);
                ART_JNI_ENTRY_OFFSET.setOffset(40);
                ART_ACCESS_FLAG_OFFSET.setOffset(4);
            } else if (apiLevel >= 23) {
                ART_QUICK_CODE_OFFSET.setOffset(48);
                ART_JNI_ENTRY_OFFSET.setOffset(40);
                ART_ACCESS_FLAG_OFFSET.setOffset(12);
            } else if (apiLevel >= 22) {
                ART_QUICK_CODE_OFFSET.setOffset(52);
                ART_JNI_ENTRY_OFFSET.setOffset(44);
                ART_ACCESS_FLAG_OFFSET.setOffset(20);
            } else if (apiLevel >= 21) {
                ART_QUICK_CODE_OFFSET.setOffset(40);
                ART_QUICK_CODE_OFFSET.setLength(BitWidth.QWORD);
                ART_JNI_ENTRY_OFFSET.setOffset(32);
                ART_JNI_ENTRY_OFFSET.setLength(BitWidth.QWORD);
                ART_ACCESS_FLAG_OFFSET.setOffset(56);
            } else {
                ART_QUICK_CODE_OFFSET.setOffset(32);
                ART_ACCESS_FLAG_OFFSET.setOffset(28);
            }
        } else {
            ART_QUICK_CODE_OFFSET.setLength(Offset.BitWidth.DWORD);
            ART_JNI_ENTRY_OFFSET.setLength(BitWidth.DWORD);
            if (apiLevel >= 26) {
                throw new RuntimeException("Unsupported now.");
            } else if (apiLevel >= 24) {
                ART_QUICK_CODE_OFFSET.setOffset(32);
                ART_JNI_ENTRY_OFFSET.setOffset(28);
                ART_ACCESS_FLAG_OFFSET.setOffset(4);
            } else if (apiLevel >= 23) {
                ART_QUICK_CODE_OFFSET.setOffset(36);
                ART_JNI_ENTRY_OFFSET.setOffset(32);
                ART_ACCESS_FLAG_OFFSET.setOffset(12);
            } else if (apiLevel >= 22) {
                ART_QUICK_CODE_OFFSET.setOffset(44);
                ART_JNI_ENTRY_OFFSET.setOffset(40);
                ART_ACCESS_FLAG_OFFSET.setOffset(20);
            } else if (apiLevel >= 21) {
                ART_QUICK_CODE_OFFSET.setOffset(40);
                ART_QUICK_CODE_OFFSET.setLength(BitWidth.QWORD);
                ART_JNI_ENTRY_OFFSET.setOffset(32);
                ART_JNI_ENTRY_OFFSET.setLength(BitWidth.QWORD);
                ART_ACCESS_FLAG_OFFSET.setOffset(56);
            } else {
                ART_QUICK_CODE_OFFSET.setOffset(32);
                ART_ACCESS_FLAG_OFFSET.setOffset(28);
            }
        }
    }
}
