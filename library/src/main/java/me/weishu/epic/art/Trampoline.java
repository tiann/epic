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

import com.taobao.android.dexposed.utility.Debug;
import com.taobao.android.dexposed.utility.Logger;
import com.taobao.android.dexposed.utility.Runtime;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import me.weishu.epic.art.arch.ShellCode;
import me.weishu.epic.art.entry.Entry;
import me.weishu.epic.art.entry.Entry64;
import me.weishu.epic.art.method.ArtMethod;

class Trampoline {
    private static final String TAG = "Trampoline";

    private final ShellCode shellCode;
    private final long jumpToAddress;
    private final byte[] originalCode;
    private int trampolineSize;
    private long trampolineAddress;
    private boolean active;

    // private ArtMethod artOrigin;
    private Set<ArtMethod> segments = new HashSet<>();

    Trampoline(ShellCode shellCode, long entryPoint) {
        this.shellCode = shellCode;
        this.jumpToAddress = shellCode.toMem(entryPoint);
        this.originalCode = EpicNative.get(jumpToAddress, shellCode.sizeOfDirectJump());
    }

    public boolean install(ArtMethod originMethod){
        boolean modified = segments.add(originMethod);
        if (!modified) {
            // Already hooked, ignore
            Logger.d(TAG, originMethod + " is already hooked, return.");
            return true;
        }

        byte[] page = create();
        EpicNative.put(page, getTrampolineAddress());

        int quickCompiledCodeSize = Epic.getQuickCompiledCodeSize(originMethod);
        int sizeOfDirectJump = shellCode.sizeOfDirectJump();
        if (quickCompiledCodeSize < sizeOfDirectJump) {
            Logger.w(TAG, originMethod.toGenericString() + " quickCompiledCodeSize: " + quickCompiledCodeSize);
            originMethod.setEntryPointFromQuickCompiledCode(getTrampolinePc());
            return true;
        }
        // 这里是绝对不能改EntryPoint的，碰到GC就挂(GC暂停线程的时候，遍历所有线程堆栈，如果被hook的方法在堆栈上，那就GG)
        // source.setEntryPointFromQuickCompiledCode(script.getTrampolinePc());
        return activate();
    }

    private long getTrampolineAddress() {
        if (getSize() != trampolineSize) {
            alloc();
        }
        return trampolineAddress;
    }

    private long getTrampolinePc() {
        return shellCode.toPC(getTrampolineAddress());
    }

    private void alloc() {
        if (trampolineAddress != 0) {
            free();
        }
        trampolineSize = getSize();
        trampolineAddress = EpicNative.map(trampolineSize);
        Logger.d(TAG, "Trampoline alloc:" + trampolineSize + ", addr: 0x" + Long.toHexString(trampolineAddress));
    }

    private void free() {
        if (trampolineAddress != 0) {
            EpicNative.unmap(trampolineAddress, trampolineSize);
            trampolineAddress = 0;
            trampolineSize = 0;
        }

        if (active) {
            EpicNative.put(originalCode, jumpToAddress);
        }
    }

    private int getSize() {
        int count = 0;
        count += shellCode.sizeOfBridgeJump() * segments.size();
        count += shellCode.sizeOfCallOrigin();
        return count;
    }

    private byte[] create() {
        Logger.d(TAG, "create trampoline." + segments);
        byte[] mainPage = new byte[getSize()];

        int offset = 0;
        for (ArtMethod method : segments) {
            byte[] bridgeJump = createTrampoline(method);
            int length = bridgeJump.length;
            System.arraycopy(bridgeJump, 0, mainPage, offset, length);
            offset += length;
        }

        byte[] callOriginal = shellCode.createCallOrigin(jumpToAddress, originalCode);
        System.arraycopy(callOriginal, 0, mainPage, offset, callOriginal.length);

        return mainPage;
    }

    private boolean activate() {
        long pc = getTrampolinePc();
        Logger.d(TAG, "Writing direct jump entry " + Debug.addrHex(pc) + " to origin entry: 0x" + Debug.addrHex(jumpToAddress));
        synchronized (Trampoline.class) {
            return EpicNative.activateNative(jumpToAddress, pc, shellCode.sizeOfDirectJump(),
                    shellCode.sizeOfBridgeJump(), shellCode.createDirectJump(pc));
        }
    }

    @Override
    protected void finalize() throws Throwable {
        free();
        super.finalize();
    }

    private byte[] createTrampoline(ArtMethod source){
        final Epic.MethodInfo methodInfo = Epic.getMethodInfo(source.getAddress());
        final Class<?> returnType = methodInfo.returnType;

//        Method bridgeMethod = Runtime.is64Bit() ? (Build.VERSION.SDK_INT == 23 ? Entry64_2.getBridgeMethod(methodInfo) : Entry64.getBridgeMethod(returnType))
//                : Entry.getBridgeMethod(returnType);
        Method bridgeMethod = Runtime.is64Bit() ? Entry64.getBridgeMethod(returnType)
                : Entry.getBridgeMethod(returnType);

        final ArtMethod target = ArtMethod.of(bridgeMethod);
        long targetAddress = target.getAddress();
        long targetEntry = target.getEntryPointFromQuickCompiledCode();
        long sourceAddress = source.getAddress();
        long structAddress = EpicNative.malloc(4);

        Logger.d(TAG, "targetAddress:"+ Debug.longHex(targetAddress));
        Logger.d(TAG, "sourceAddress:"+ Debug.longHex(sourceAddress));
        Logger.d(TAG, "targetEntry:"+ Debug.longHex(targetEntry));
        Logger.d(TAG, "structAddress:"+ Debug.longHex(structAddress));

        return shellCode.createBridgeJump(targetAddress, targetEntry, sourceAddress, structAddress);
    }
}
