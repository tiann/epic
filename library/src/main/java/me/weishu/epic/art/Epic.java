/*
 * Copyright (c) 2017, weishu
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

import android.os.Build;
import android.util.Log;

import com.taobao.android.dexposed.utility.Debug;
import com.taobao.android.dexposed.utility.Logger;
import com.taobao.android.dexposed.utility.Runtime;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import me.weishu.epic.art.arch.Arm64;
import me.weishu.epic.art.arch.Arm64ForM;
import me.weishu.epic.art.arch.ShellCode;
import me.weishu.epic.art.arch.Thumb2;
import me.weishu.epic.art.method.ArtMethod;

/**
 * Hook Center.
 */
public final class Epic {

    private static final String TAG = "Epic";

    private static final Map<String, List<ArtMethod>> backupMethodsMapping = new ConcurrentHashMap<String, List<ArtMethod>>();

    private static final Map<Long, MethodInfo> originSigs = new HashMap<>();

    private static final Map<String, Trampoline> scripts = new HashMap<>();
    private static ShellCode ShellCode;

    static {
        boolean isArm = true; // TODO: 17/11/21 TODO
        if (isArm) {
            if (Runtime.is64Bit()) {
                if (Build.VERSION.SDK_INT == 23) {
                    ShellCode = new Arm64ForM();
                } else if (Build.VERSION.SDK_INT == 24 || Build.VERSION.SDK_INT == 25) {
                    ShellCode = new Arm64();
                }
            } else if (Runtime.isThumb2()) {
                ShellCode = new Thumb2();
            } else {
                // todo ARM32
            }
        }
        if (ShellCode == null) {
            throw new RuntimeException("Do not support this ARCH now!! API LEVEL:" + Build.VERSION.SDK_INT);
        }
        Logger.d(TAG, "Using: " + ShellCode.getName());
    }

    public static boolean hookMethod(Constructor origin) {
        return hookMethod(ArtMethod.of(origin));
    }

    public static boolean hookMethod(Method origin) {
        ArtMethod artOrigin = ArtMethod.of(origin);
        return hookMethod(artOrigin);
    }

    private static boolean hookMethod(ArtMethod artOrigin) {

        MethodInfo methodInfo = new MethodInfo();
        methodInfo.isStatic = Modifier.isStatic(artOrigin.getModifiers());
        final Class<?>[] parameterTypes = artOrigin.getParameterTypes();
        if (parameterTypes != null) {
            methodInfo.paramNumber = parameterTypes.length;
            methodInfo.paramTypes = parameterTypes;
        } else {
            methodInfo.paramNumber = 0;
            methodInfo.paramTypes = new Class<?>[0];
        }
        methodInfo.returnType = artOrigin.getReturnType();
        methodInfo.method = artOrigin;
        originSigs.put(artOrigin.getAddress(), methodInfo);

        artOrigin.ensureResolved();

        String identifier = artOrigin.getIdentifier();

        final long originEntry = artOrigin.getEntryPointFromQuickCompiledCode();
        if (originEntry == ArtMethod.getQuickToInterpreterBridge()) {
            Logger.w(TAG, "this method is not compiled, compile it now. current entry: 0x" + Long.toHexString(originEntry));
            boolean ret = artOrigin.compile();
            if (ret) {
                Logger.i(TAG, "compile method success, new entry: 0x" + Long.toHexString(artOrigin.getEntryPointFromQuickCompiledCode()));
            } else {
                Logger.e(TAG, "compile method failed...");
                return false;
                // return hookInterpreterBridge(artOrigin);
            }
        }

        ArtMethod backupMethod = artOrigin.backup();

        Logger.i(TAG, "backup method address:" + Debug.addrHex(backupMethod.getAddress()));
        Logger.i(TAG, "backup method entry :" + Debug.addrHex(backupMethod.getEntryPointFromQuickCompiledCode()));

        List<ArtMethod> backupList = backupMethodsMapping.get(identifier);
        if (backupList == null) {
            backupList = new LinkedList<ArtMethod>();
            backupMethodsMapping.put(identifier, backupList);
        }
        backupList.add(backupMethod);

        if (!scripts.containsKey(identifier)) {
            scripts.put(identifier, new Trampoline(ShellCode, artOrigin));
        }
        Trampoline trampoline = scripts.get(identifier);

        boolean ret = trampoline.install();
        Logger.i(TAG, "hook Method result:" + ret);
        return ret;
    }

    /*
    private static boolean hookInterpreterBridge(ArtMethod artOrigin) {

        String identifier = artOrigin.getIdentifier();
        ArtMethod backupMethod = artOrigin.backup();

        Logger.d(TAG, "backup method address:" + Debug.addrHex(backupMethod.getAddress()));
        Logger.d(TAG, "backup method entry :" + Debug.addrHex(backupMethod.getEntryPointFromQuickCompiledCode()));

        List<ArtMethod> backupList = backupMethodsMapping.get(identifier);
        if (backupList == null) {
            backupList = new LinkedList<ArtMethod>();
            backupMethodsMapping.put(identifier, backupList);
        }
        backupList.add(backupMethod);

        long originalEntryPoint = ShellCode.toMem(artOrigin.getEntryPointFromQuickCompiledCode());
        Logger.d(TAG, "originEntry Point(bridge):" + Debug.addrHex(originalEntryPoint));

        originalEntryPoint += 16;
        Logger.d(TAG, "originEntry Point(offset8):" + Debug.addrHex(originalEntryPoint));

        if (!scripts.containsKey(originalEntryPoint)) {
            scripts.put(originalEntryPoint, new Trampoline(ShellCode, artOrigin));
        }
        Trampoline trampoline = scripts.get(originalEntryPoint);

        boolean ret = trampoline.install();
        Logger.i(TAG, "hook Method result:" + ret);
        return ret;

    }*/

    public static ArtMethod getBackMethod(ArtMethod origin) {
        String identifier = origin.getIdentifier();
        List<ArtMethod> backupList = backupMethodsMapping.get(identifier);
        if (backupList == null) {
            return null;
        }

        return backupList.get(backupList.size() - 1);
    }

    public static MethodInfo getMethodInfo(long address) {
        return originSigs.get(address);
    }

    public static int getQuickCompiledCodeSize(ArtMethod method) {

        long entryPoint = ShellCode.toMem(method.getEntryPointFromQuickCompiledCode());
        long sizeInfo1 = entryPoint - 4;
        byte[] bytes = EpicNative.get(sizeInfo1, 4);
        int size = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
        Log.i(TAG, "getQuickCompiledCodeSize: " + size);
        return size;
    }


    public static class MethodInfo {
        public boolean isStatic;
        public int paramNumber;
        public Class<?>[] paramTypes;
        public Class<?> returnType;
        public ArtMethod method;

        @Override
        public String toString() {
            return "MethodInfo{" +
                    "isStatic=" + isStatic +
                    ", paramNumber=" + paramNumber +
                    ", paramTypes=" + Arrays.toString(paramTypes) +
                    ", returnType=" + returnType +
                    '}';
        }
    }
}
