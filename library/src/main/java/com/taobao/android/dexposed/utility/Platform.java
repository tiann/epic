/*
 * Original work Copyright (c) 2016, Lody
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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class Platform {

    /*package*/ static Platform PLATFORM_INTERNAL;

    static {
        if (Runtime.is64Bit()) {
            PLATFORM_INTERNAL = new Platform64Bit();
        }else {
            PLATFORM_INTERNAL = new Platform32Bit();
        }
    }

    public static Platform getPlatform() {
        return PLATFORM_INTERNAL;
    }

    /**
     * Convert a byte array to int,
     * Use this function to get address from memory.
     *
     * @param data byte array
     * @return long
     */
    public abstract int orderByteToInt(byte[] data);

    /**
     * Convert a byte array to long,
     * Use this function to get address from memory.
     *
     * @param data byte array
     * @return long
     */
    public abstract long orderByteToLong(byte[] data);

    public abstract byte[] orderLongToByte(long serial, int length);

    public abstract byte[] orderIntToByte(int serial);

    public abstract int getIntSize();


    static class Platform32Bit extends Platform {

        @Override
        public int orderByteToInt(byte[] data) {
            return ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getInt();
        }

        @Override
        public long orderByteToLong(byte[] data) {
            return ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getInt() & 0xFFFFFFFFL;
        }

        @Override
        public byte[] orderLongToByte(long serial, int length) {
            return ByteBuffer.allocate(length).order(ByteOrder.LITTLE_ENDIAN).putInt((int) serial).array();
        }

        @Override
        public byte[] orderIntToByte(int serial) {
            return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(serial).array();
        }

        @Override
        public int getIntSize() {
            return 4;
        }


    }

    static class Platform64Bit extends Platform {

        @Override
        public int orderByteToInt(byte[] data) {
            return ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getInt();
        }

        @Override
        public long orderByteToLong(byte[] data) {
            return ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getLong();
        }

        @Override
        public byte[] orderLongToByte(long serial, int length) {
            return ByteBuffer.allocate(length).order(ByteOrder.LITTLE_ENDIAN).putLong(serial).array();
        }

        @Override
        public byte[] orderIntToByte(int serial) {
            return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(serial).array();
        }

        @Override
        public int getIntSize() {
            return 8;
        }
    }

}
