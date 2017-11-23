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

public class Arm64 extends ShellCode {

    @Override
    public int sizeOfDirectJump() {
        return 4 * 4;
    }

    @Override
    public byte[] createDirectJump(long targetAddress) {
        byte[] instructions = new byte[]{
                0x50, 0x00, 0x00, 0x58,         // ldr x9, _targetAddress
                0x00, 0x02, 0x1F, (byte) 0xD6,  // br x9
                0x00, 0x00, 0x00, 0x00,         // targetAddress
                0x00, 0x00, 0x00, 0x00          // targetAddress
        };
        writeLong(targetAddress, ByteOrder.LITTLE_ENDIAN, instructions, instructions.length - 8);
        return instructions;
    }

    @Override
    public byte[] createBridgeJump(long targetAddress, long targetEntry, long srcAddress, long structAddress) {

        byte[] instructions = new byte[]{
                0x1f, 0x20, 0x03, (byte) 0xd5,         // nop
                0x69, 0x02, 0x00, 0x58,                // ldr x9, source_method
                0x1f, 0x00, 0x09, (byte) 0xeb,         // cmp x0, x9
                (byte) 0xa1, 0x02, 0x00, 0x54,         // bne 5f
                (byte) 0x80, 0x01, 0x00, 0x58,         // ldr x0, target_method

                0x29, 0x02, 0x00, 0x58,                // ldr x9, struct
                (byte) 0xea, 0x03, 0x00, (byte) 0x91,  // mov x10, sp

                0x2a, 0x01, 0x00, (byte) 0xf9,         // str x10, [x9, #0]
                0x22, 0x05, 0x00, (byte) 0xf9,         // str x2, [x9, #8]

                0x23, 0x09, 0x00, (byte) 0xf9,         // str x3, [x9, #16]
                (byte) 0xe3, 0x03, 0x09, (byte) 0xaa,  // mov x3, x9
                0x22, 0x01, 0x00, 0x58,                // ldr x2, source_method
                0x22, 0x0d, 0x00, (byte) 0xf9,         // str x2, [x9, #24]
                (byte) 0xe2, 0x03, 0x13, (byte) 0xaa,  // mov x2, x19
                (byte) 0x89, 0x00, 0x00, 0x58,         // ldr x9, target_method_entry
                0x20, 0x01, 0x1f, (byte) 0xd6,         // br x9

                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // target_method_address
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // target_method_entry
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // source_method
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00  // struct

        };


        writeLong(targetAddress, ByteOrder.LITTLE_ENDIAN, instructions,
                instructions.length - 32);
        writeLong(targetEntry, ByteOrder.LITTLE_ENDIAN, instructions,
                instructions.length - 24);
        writeLong(srcAddress,
                ByteOrder.LITTLE_ENDIAN, instructions, instructions.length - 16);
        writeLong(structAddress, ByteOrder.LITTLE_ENDIAN, instructions,
                instructions.length - 8);

        return instructions;
    }

    @Override
    public int sizeOfBridgeJump() {
        return 24 * 4;
    }


    @Override
    public long toPC(long code) {
        return code;
    }

    @Override
    public long toMem(long pc) {
        return pc;
    }

    @Override
    public String getName() {
        return "64-bit ARM";
    }
}
