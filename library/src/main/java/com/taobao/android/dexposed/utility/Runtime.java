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

import android.annotation.SuppressLint;

import java.lang.reflect.Method;

import me.weishu.epic.art.method.ArtMethod;

public class Runtime {

    private static Boolean g64 = null;

    public static boolean is64Bit() {
        if (g64 == null)
            try {
                g64 = (Boolean) Class.forName("dalvik.system.VMRuntime").getDeclaredMethod("is64Bit").invoke(Class.forName("dalvik.system.VMRuntime").getDeclaredMethod("getRuntime").invoke(null));
            } catch (Exception e) {
                g64 = Boolean.FALSE;
            }
        return g64;
    }

    public static boolean isArt() {
        return System.getProperty("java.vm.version").startsWith("2");
    }

    public static boolean isThumb2() {
        boolean isThumb2 = false;
        try {
            Method method = ArtMethod.class.getDeclaredMethod("of", Method.class);
            ArtMethod artMethodStruct = ArtMethod.of(method);
            isThumb2 = ((artMethodStruct.getEntryPointFromQuickCompiledCode() & 1) == 1);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return isThumb2;
    }

    public static boolean isYunOS() {
        String version = null;
        String vmName = null;

        try {
            @SuppressLint("PrivateApi") Method m = Class.forName("android.os.SystemProperties").getMethod("get", String.class);
            version = (String) m.invoke(null, "ro.yunos.version");
            vmName = (String) m.invoke(null, "java.vm.name");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return vmName != null && vmName.toLowerCase().contains("lemur")
                || version != null && version.trim().length() > 0;
    }
}
