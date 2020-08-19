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
#include "art.h"
#include <android/log.h>
#include <cstddef>

#define LOGV(...) ((void)__android_log_print(ANDROID_LOG_INFO, "epic.Native", __VA_ARGS__))

#define ANDROID_R_API 30
#define MAX_SEARCH_LEN 2000

void* ArtHelper::runtime_instance_ = nullptr;
int ArtHelper::api = 0;

template <typename T>
inline int find_offset(void* hay, int size, T needle)
{
  for (int i = 0; i < size; i += sizeof(T)) {
    auto current = reinterpret_cast<T*>(reinterpret_cast<char*>(hay) + i);
    if (*current == needle) {
      return i;
    }
  }
  return -1;
}

void ArtHelper::init(JNIEnv* env, int api)
{
  ArtHelper::api = api;
  JavaVM* javaVM;
  env->GetJavaVM(&javaVM);
  JavaVMExt* javaVMExt = (JavaVMExt*)javaVM;

  void* runtime = javaVMExt->runtime;
  if (runtime == nullptr) {
    return;
  }

  if (api < ANDROID_R_API) {
    runtime_instance_ = runtime;
  } else {
    int vm_offset = find_offset(runtime, MAX_SEARCH_LEN, javaVM);
    runtime_instance_ = reinterpret_cast<void*>(reinterpret_cast<char*>(runtime) + vm_offset - offsetof(PartialRuntimeR, java_vm_));
  }
}

void* ArtHelper::getClassLinker()
{
  if (runtime_instance_ == nullptr || api < ANDROID_R_API) {
    return nullptr;
  }
  PartialRuntimeR* runtimeR = (PartialRuntimeR*)runtime_instance_;
  return runtimeR->class_linker_;
}

void* ArtHelper::getJniIdManager()
{
  if (runtime_instance_ == nullptr || api < ANDROID_R_API) {
    return nullptr;
  }
  PartialRuntimeR* runtimeR = (PartialRuntimeR*)runtime_instance_;
  return runtimeR->jni_id_manager_;
}

void* ArtHelper::getJitCodeCache()
{
  if (runtime_instance_ == nullptr || api < ANDROID_R_API) {
    return nullptr;
  }
  PartialRuntimeR* runtimeR = (PartialRuntimeR*)runtime_instance_;
  return runtimeR->jit_code_cache_;
}

void* ArtHelper::getHeap()
{
  if (runtime_instance_ == nullptr) {
    return nullptr;
  }
  if (api < 26) {
    Runtime_7X* runtime7X = (Runtime_7X*)runtime_instance_;
    return runtime7X->heap_;
  } else if (api < ANDROID_R_API) {
    Runtime_8X* runtime8X = (Runtime_8X*)runtime_instance_;
    LOGV("bootclasspath : %s", runtime8X->boot_class_path_string_.c_str());
    return runtime8X->heap_;
  } else {
    PartialRuntimeR* runtimeR = (PartialRuntimeR*)runtime_instance_;
    return runtimeR->heap_;
  }
}
