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

package me.weishu.epic.art.arch;

import java.nio.ByteOrder;


public class Thumb2_2 extends ShellCode {

    @Override
    public int sizeOfDirectJump() {
        return 8;
    }

    @Override
    public byte[] createDirectJump(long targetAddress) {
        byte[] instructions = new byte[] {
                (byte) 0xdf, (byte) 0xf8, 0x00, (byte) 0xf0,        // ldr pc, [pc]
                0, 0, 0, 0
        };
        writeInt((int) targetAddress, ByteOrder.LITTLE_ENDIAN, instructions,
                instructions.length - 4);
        return instructions;
    }

    @Override
    public byte[] createBridgeJump(long targetAddress, long targetEntry, long srcAddress, long structAddress) {
        final byte[] instructions = new byte[] {
                (byte) 0xdf, (byte) 0xf8, 0x14, (byte) 0xc0,    // ldr ip, [pc, #20]
                (byte) 0x60, 0x45,                              // cmp r0, ip
                0x40, (byte) 0xf0, 0x09, (byte) 0x80,           // bne next
                0x01, 0x48,                                     // ldr r0, [pc, #4]
                (byte) 0xdf, (byte) 0xf8, 0x04, (byte) 0xf0,    // ldr pc, [pc, #4]
                0x0, 0x0, 0x0, 0x0,                             // targetAddress
                0x0, 0x0, 0x0, 0x0,                             // entryPointFromQuickCompiledCode
                0x0, 0x0, 0x0, 0x0,                             // srcAddress
        };
        writeInt((int) targetAddress, ByteOrder.LITTLE_ENDIAN, instructions,
                instructions.length - 12);
        writeInt((int) targetEntry,
                ByteOrder.LITTLE_ENDIAN, instructions, instructions.length - 8);
        writeInt((int) srcAddress, ByteOrder.LITTLE_ENDIAN, instructions,
                instructions.length - 4);
        return instructions;
    }

    @Override
    public int sizeOfBridgeJump() {
        // return 15 * 4;
        return 28;
    }

    @Override
    public long toPC(long code) {
        return toMem(code) + 1;
    }

    @Override
    public long toMem(long pc) {
        return pc & ~0x1;
    }

    @Override
    public String getName() {
        return "Thumb2";
    }
}
