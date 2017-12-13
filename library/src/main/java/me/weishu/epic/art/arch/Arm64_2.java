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

public class Arm64_2 extends ShellCode {

    @Override
    public int sizeOfDirectJump() {
        return 4 * 4;
    }

    @Override
    public byte[] createDirectJump(long targetAddress) {
        byte[] instructions = new byte[]{
                0x50, 0x00, 0x00, 0x58,         // ldr x16, _targetAddress
                0x00, 0x02, 0x1F, (byte) 0xD6,  // br x16
                0x00, 0x00, 0x00, 0x00,         // targetAddress
                0x00, 0x00, 0x00, 0x00          // targetAddress
        };
        writeLong(targetAddress, ByteOrder.LITTLE_ENDIAN, instructions, instructions.length - 8);
        return instructions;
    }

    @Override
    public byte[] createBridgeJump(long targetAddress, long targetEntry, long srcAddress, long structAddress) {

        byte[] instructions = new byte[] {

                    /*
                    ldr x17, 3f
                    cmp x0, x17
                    bne 5f
                    ldr x0, 1f
                    ldr x17, 4f
                    mov x16, sp
                    str x16, [x17, #0]
                    str x2, [x17, #8]
                    ldr x16, 3f
                    str x16, [x17, #16]
                    mov x2, x17
                    ldr x17, 2f
                    br x17

                    1:
                    .quad 0x0
                    2:
                    .quad 0x0
                    3:
                    .quad 0x0
                    4:
                    .quad 0x0

                    5:
                    mov x0, x17

                    */
                    0x1f, 0x20, 0x03, (byte) 0xd5,         // nop
                    0x31, 0x02, 0x00, 0x58,
                    0x1f, 0x00, 0x11, (byte)0xeb,
                    (byte)0x61, 0x02, 0x00, 0x54,
                    (byte)0x40, 0x01, 0x00, 0x58,
                    (byte)0xf1, 0x01, 0x00, 0x58,
                    (byte)0xf0, 0x03, 0x00, (byte)0x91,
                    (byte)0x30, 0x02, 0x00, (byte)0xf9,
                    0x22, 0x06, 0x00, (byte)0xf9,
                    0x30, 0x01, 0x00, (byte)0x58,
                    (byte)0x30, 0x0a, 0x00, (byte)0xf9,
                    (byte)0xe2, 0x03, 0x11, (byte)0xaa,
                    (byte)0x91, 0x00, 0x00, 0x58,
                    (byte)0x20, 0x02, 0x1f, (byte)0xd6,

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
        return 22 * 4;
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
        return "64-bit ARM(Android M)";
    }
}
