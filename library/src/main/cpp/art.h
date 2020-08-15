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

#ifndef EPIC_ART_H
#define EPIC_ART_H

#include <jni.h>
#include <list>
#include <stdint.h>
#include <string>
#include <vector>

// Android 6.0: http://androidxref.com/6.0.0_r5/xref/art/runtime/runtime.h
// Android 7.0: http://androidxref.com/7.0.0_r1/xref/art/runtime/runtime.h
// Android 7.1.1: http://androidxref.com/7.1.1_r6/xref/art/runtime/runtime.h
// Android 8.0: http://androidxref.com/8.0.0_r4/xref/art/runtime/runtime.h

struct Runtime_7X {

    uint64_t callee_save_methods_[3];
    void* pre_allocated_OutOfMemoryError_;
    void* pre_allocated_NoClassDefFoundError_;
    void* resolution_method_;
    void* imt_conflict_method_;
    // Unresolved method has the same behavior as the conflict method, it is used by the class linker
    // for differentiating between unfilled imt slots vs conflict slots in superclasses.
    void* imt_unimplemented_method_;
    void* sentinel_;

    int instruction_set_;
    uint32_t callee_save_method_frame_infos_[9]; // QuickMethodFrameInfo = uint32_t * 3

    void* compiler_callbacks_;
    bool is_zygote_;
    bool must_relocate_;
    bool is_concurrent_gc_enabled_;
    bool is_explicit_gc_disabled_;
    bool dex2oat_enabled_;
    bool image_dex2oat_enabled_;

    std::string compiler_executable_;
    std::string patchoat_executable_;
    std::vector<std::string> compiler_options_;
    std::vector<std::string> image_compiler_options_;
    std::string image_location_;

    std::string boot_class_path_string_;
    std::string class_path_string_;
    std::vector<std::string> properties_;

    // The default stack size for managed threads created by the runtime.
    size_t default_stack_size_;

    void* heap_;
};

struct Runtime_8X {

    uint64_t callee_save_methods_[3];
    void* pre_allocated_OutOfMemoryError_;
    void* pre_allocated_NoClassDefFoundError_;
    void* resolution_method_;
    void* imt_conflict_method_;
    // Unresolved method has the same behavior as the conflict method, it is used by the class linker
    // for differentiating between unfilled imt slots vs conflict slots in superclasses.
    void* imt_unimplemented_method_;
    void* sentinel_;

    int instruction_set_;
    uint32_t callee_save_method_frame_infos_[9]; // QuickMethodFrameInfo = uint32_t * 3

    void* compiler_callbacks_;
    bool is_zygote_;
    bool must_relocate_;
    bool is_concurrent_gc_enabled_;
    bool is_explicit_gc_disabled_;
    bool dex2oat_enabled_;
    bool image_dex2oat_enabled_;

    std::string compiler_executable_;
    std::string patchoat_executable_;
    std::vector<std::string> compiler_options_;
    std::vector<std::string> image_compiler_options_;
    std::string image_location_;

    std::string boot_class_path_string_;
    std::string class_path_string_;
    std::vector<std::string> properties_;

    std::list<void*> agents_;
    std::vector<void*> plugins_;

    // The default stack size for managed threads created by the runtime.
    size_t default_stack_size_;

    void* heap_;
};

struct PartialRuntimeR {
  void* heap_;

  void* jit_arena_pool_;
  void* arena_pool_;
  // Special low 4gb pool for compiler linear alloc. We need ArtFields to be in low 4gb if we are
  // compiling using a 32 bit image on a 64 bit compiler in case we resolve things in the image
  // since the field arrays are int arrays in this case.
  void* low_4gb_arena_pool_;

  // Shared linear alloc for now.
  void* linear_alloc_;

  // The number of spins that are done before thread suspension is used to forcibly inflate.
  size_t max_spins_before_thin_lock_inflation_;
  void* monitor_list_;
  void* monitor_pool_;

  void* thread_list_;

  void* intern_table_;

  void* class_linker_;

  void* signal_catcher_;

  void* jni_id_manager_;

  void* java_vm_;

  void* jit_;
  void* jit_code_cache_;
};

struct JavaVMExt {
    void* functions;
    void* runtime;
};

class ArtHelper {
  public:
    static void init(JNIEnv*, int);
    static void* getRuntimeInstance() { return runtime_instance_; }
    static void* getJniIdManager();
    static void* getHeap();

  private:
    static void* runtime_instance_;
    static int api;
};

#endif //EPIC_ART_H
