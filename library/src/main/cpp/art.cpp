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

#include <android/log.h>
#include "art.h"

#define LOGV(...)  ((void)__android_log_print(ANDROID_LOG_INFO, "epic.Native", __VA_ARGS__))

void* getHeap(JNIEnv* env, int api) {
    JavaVM* javaVM;
    env->GetJavaVM(&javaVM);
    JavaVMExt *javaVMExt = (JavaVMExt *) javaVM;

    void *runtime = javaVMExt->runtime;
    if (runtime == nullptr) {
        return nullptr;
    }

    if (api < 26) {
        Runtime_7X *runtime7X = (Runtime_7X *) runtime;
        return runtime7X->heap_;
    } else {
        Runtime_8X *runtime8X = (Runtime_8X *) runtime;
        LOGV("bootclasspath : %s", runtime8X->boot_class_path_string_.c_str());
        return runtime8X->heap_;
    }
}

