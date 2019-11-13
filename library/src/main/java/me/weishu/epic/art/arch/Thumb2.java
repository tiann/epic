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


public class Thumb2 extends ShellCode {

    @Override
    public int sizeOfDirectJump() {
        return 12;
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
        // 有问题，参数丢失。
//        byte[] instructions = new byte[] {
//                (byte) 0xdf, (byte) 0xf8, 0x18, (byte) 0xc0,    // ldr ip, [pc, #24], ip = source_method_address
//
//                (byte) 0x01, (byte) 0x91,                       // str r1, [sp, #4]
//                (byte) 0x02, (byte) 0x92,                       // str r2, [sp, #8]
//
//                (byte) 0x03, (byte) 0x93,                       // str r3, [sp, #12]
//                (byte) 0x62, (byte) 0x46,                       // mov r2, ip
//
//                0x01, 0x48,                                     // ldr r0, [pc, #4]
//                0x6b, 0x46,                                     // mov r3, sp
//
//                (byte) 0xdf, (byte) 0xf8, 0x04, (byte) 0xf0,    // ldr pc, [pc, #4]
//
//                0x0, 0x0, 0x0, 0x0,                             // target_method_pos_x
//                0x0, 0x0, 0x0, 0x0,                             // target_method_entry_point
//                0x0, 0x0, 0x0, 0x0,                             // src_method_pos_x
//        };
//        writeInt((int) targetAddress, ByteOrder.LITTLE_ENDIAN, instructions,
//                instructions.length - 12);
//        writeInt((int) targetEntry,
//                ByteOrder.LITTLE_ENDIAN, instructions, instructions.length - 8);
//        writeInt((int) srcAddress, ByteOrder.LITTLE_ENDIAN, instructions,
//                instructions.length - 4);

        byte[] instructions = new byte[]{

                (byte) 0xdf, (byte) 0xf8, (byte) 0x30, (byte) 0xc0, // ldr ip, [pc, #48] ip = source method address

                (byte) 0x60, (byte) 0x45,                           // cmp r0, ip        if r0 != ip
                (byte) 0x40, (byte) 0xf0, (byte) 0x19, (byte) 0x80, // bne.w 1f          jump label 1:
                (byte) 0x08, (byte) 0x48,                           // ldr r0, [pc, #28] r0 = target_method_address
                (byte) 0xdf, (byte) 0xf8, (byte) 0x28, (byte) 0xc0, // ldr ip, [pc, #38] ip = struct address
                (byte) 0xcc, (byte) 0xf8, (byte) 0x00, (byte) 0xd0, // str sp, [ip, #0]
                (byte) 0xcc, (byte) 0xf8, (byte) 0x04, (byte) 0x20, // str r2, [ip, #4]
                (byte) 0xcc, (byte) 0xf8, (byte) 0x08, (byte) 0x30, // str r3, [ip, #8]

                (byte) 0x63, (byte) 0x46,                           // mov r3, ip
                (byte) 0x05, (byte) 0x4a,                           // ldr r2, [pc, #16] r2 = source_method_address
                (byte) 0xcc, (byte) 0xf8, (byte) 0x0c, (byte) 0x20, // str r2, [ip, #12]
                (byte) 0x4a, (byte) 0x46,                           // move r2, r9
                (byte) 0x4a, (byte) 0x46,                           // move r2, r9

                (byte) 0xdf, (byte) 0xf8, (byte) 0x04, (byte) 0xf0, // ldr pc, [pc, #4]

                0x0, 0x0, 0x0, 0x0,                             // target_method_pos_x
                0x0, 0x0, 0x0, 0x0,                             // target_method_entry_point
                0x0, 0x0, 0x0, 0x0,                             // src_method_address
                0x0, 0x0, 0x0, 0x0,                             // struct address (sp, r1, r2)
                // 1:
        };

        writeInt((int) targetAddress, ByteOrder.LITTLE_ENDIAN, instructions,
                instructions.length - 16);
        writeInt((int) targetEntry, ByteOrder.LITTLE_ENDIAN, instructions,
                instructions.length - 12);
        writeInt((int) srcAddress,
                ByteOrder.LITTLE_ENDIAN, instructions, instructions.length - 8);
        writeInt((int) structAddress, ByteOrder.LITTLE_ENDIAN, instructions,
                instructions.length - 4);

        return instructions;
    }

    @Override
    public int sizeOfBridgeJump() {
        return 15 * 4;
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
