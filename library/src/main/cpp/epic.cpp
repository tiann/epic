/*
 * Original Copyright 2014-2015 Marvin Wißfeld
 * Modified work Copyright (c) 2017, weishu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <jni.h>
#include <android/log.h>
#include <sys/mman.h>
#include <errno.h>
#include <unistd.h>
#include <dlfcn.h>
#include <cstdlib>
#include <sys/system_properties.h>
#include <fcntl.h>
#include "fake_dlfcn.h"
#include "art.h"

#undef NDEBUG
#ifdef NDEBUG
#define LOGV(...)  ((void)__android_log_print(ANDROID_LOG_INFO, "epic.Native", __VA_ARGS__))
#else
#define LOGV(...)
#endif

#define JNIHOOK_CLASS "me/weishu/epic/art/EpicNative"

jobject (*addWeakGloablReference)(JavaVM *, void *, void *) = nullptr;

void* (*jit_load_)(bool*) = nullptr;
void* jit_compiler_handle_ = nullptr;
bool (*jit_compile_method_)(void*, void*, void*, bool) = nullptr;

typedef bool (*JIT_COMPILE_METHOD1)(void *, void *, void *, bool);
typedef bool (*JIT_COMPILE_METHOD2)(void *, void *, void *, bool, bool); // Android Q

void (*jit_unload_)(void*) = nullptr;

class ScopedSuspendAll {};

void (*suspendAll)(ScopedSuspendAll*, char*) = nullptr;
void (*resumeAll)(ScopedSuspendAll*) = nullptr;

class ScopedJitSuspend {};
void (*startJit)(ScopedJitSuspend*) = nullptr;
void (*stopJit)(ScopedJitSuspend*) = nullptr;

void (*DisableMovingGc)(void*) = nullptr;

void* (*JniIdManager_DecodeMethodId_)(void*, jlong) = nullptr;

void* __self() {

#ifdef __arm__
    register uint32_t r9 asm("r9");
    return (void*) r9;
#elif defined(__aarch64__)
    register uint64_t x19 asm("x19");
    return (void*) x19;
#else
#endif

};

static int api_level;

void init_entries(JNIEnv *env) {
    char api_level_str[5];
    __system_property_get("ro.build.version.sdk", api_level_str);
    api_level = atoi(api_level_str);
    LOGV("api level: %d", api_level);
    ArtHelper::init(env, api_level);
    if (api_level < 23) {
        // Android L, art::JavaVMExt::AddWeakGlobalReference(art::Thread*, art::mirror::Object*)
        void *handle = dlopen("libart.so", RTLD_LAZY | RTLD_GLOBAL);
        addWeakGloablReference = (jobject (*)(JavaVM *, void *, void *)) dlsym(handle,
                                                                               "_ZN3art9JavaVMExt22AddWeakGlobalReferenceEPNS_6ThreadEPNS_6mirror6ObjectE");
    } else if (api_level < 24) {
        // Android M, art::JavaVMExt::AddWeakGlobalRef(art::Thread*, art::mirror::Object*)
        void *handle = dlopen("libart.so", RTLD_LAZY | RTLD_GLOBAL);
        addWeakGloablReference = (jobject (*)(JavaVM *, void *, void *)) dlsym(handle,
                                                                               "_ZN3art9JavaVMExt16AddWeakGlobalRefEPNS_6ThreadEPNS_6mirror6ObjectE");
    } else {
        // Android N and O, Google disallow us use dlsym;
        void *handle = dlopen_ex("libart.so", RTLD_NOW);
        void *jit_lib = dlopen_ex("libart-compiler.so", RTLD_NOW);
        LOGV("fake dlopen install: %p", handle);
        const char *addWeakGloablReferenceSymbol = api_level <= 25
                                                   ? "_ZN3art9JavaVMExt16AddWeakGlobalRefEPNS_6ThreadEPNS_6mirror6ObjectE"
                                                   : "_ZN3art9JavaVMExt16AddWeakGlobalRefEPNS_6ThreadENS_6ObjPtrINS_6mirror6ObjectEEE";
        addWeakGloablReference = (jobject (*)(JavaVM *, void *, void *)) dlsym_ex(handle, addWeakGloablReferenceSymbol);

        jit_compile_method_ = (bool (*)(void *, void *, void *, bool)) dlsym_ex(jit_lib, "jit_compile_method");
        jit_load_ = reinterpret_cast<void* (*)(bool*)>(dlsym_ex(jit_lib, "jit_load"));
        bool generate_debug_info = false;
        jit_compiler_handle_ = (jit_load_)(&generate_debug_info);
        LOGV("jit compile_method: %p", jit_compile_method_);

        suspendAll = reinterpret_cast<void (*)(ScopedSuspendAll*, char*)>(dlsym_ex(handle, "_ZN3art16ScopedSuspendAllC1EPKcb"));
        resumeAll = reinterpret_cast<void (*)(ScopedSuspendAll*)>(dlsym_ex(handle, "_ZN3art16ScopedSuspendAllD1Ev"));

        if (api_level >= 30) {
            // Android R would not directly return ArtMethod address but an internal id
            JniIdManager_DecodeMethodId_ = reinterpret_cast<void* (*)(void*, jlong)>(dlsym_ex(handle, "_ZN3art3jni12JniIdManager14DecodeMethodIdEP10_jmethodID"));
        }
        // Disable this now.
        // startJit = reinterpret_cast<void(*)(ScopedJitSuspend*)>(dlsym_ex(handle, "_ZN3art3jit16ScopedJitSuspendD1Ev"));
        // stopJit = reinterpret_cast<void(*)(ScopedJitSuspend*)>(dlsym_ex(handle, "_ZN3art3jit16ScopedJitSuspendC1Ev"));

        // DisableMovingGc = reinterpret_cast<void(*)(void*)>(dlsym_ex(handle, "_ZN3art2gc4Heap15DisableMovingGcEv"));
    }

    LOGV("addWeakGloablReference: %p", addWeakGloablReference);
}

jboolean epic_compile(JNIEnv *env, jclass, jobject method, jlong self) {
    LOGV("self from native peer: %p, from register: %p", reinterpret_cast<void*>(self), __self());
    jlong art_method = (jlong) env->FromReflectedMethod(method);
    if (art_method % 2 == 1) {
        art_method = reinterpret_cast<jlong>(JniIdManager_DecodeMethodId_(ArtHelper::getJniIdManager(), art_method));
    }
    bool ret;
    if (api_level >= 29) {
        ret = ((JIT_COMPILE_METHOD2) jit_compile_method_)(jit_compiler_handle_,
                                                          reinterpret_cast<void *>(art_method),
                                                          reinterpret_cast<void *>(self), false, false);
    } else {
        ret = ((JIT_COMPILE_METHOD1) jit_compile_method_)(jit_compiler_handle_,
                                                          reinterpret_cast<void *>(art_method),
                                                          reinterpret_cast<void *>(self), false);
    }
    return (jboolean)ret;
}

jlong epic_suspendAll(JNIEnv *, jclass) {
    ScopedSuspendAll *scopedSuspendAll = (ScopedSuspendAll *) malloc(sizeof(ScopedSuspendAll));
    suspendAll(scopedSuspendAll, "stop_jit");
    return reinterpret_cast<jlong >(scopedSuspendAll);
}

void epic_resumeAll(JNIEnv* env, jclass, jlong obj) {
    ScopedSuspendAll* scopedSuspendAll = reinterpret_cast<ScopedSuspendAll*>(obj);
    resumeAll(scopedSuspendAll);
}

jlong epic_stopJit(JNIEnv*, jclass) {
    ScopedJitSuspend *scopedJitSuspend = (ScopedJitSuspend *) malloc(sizeof(ScopedJitSuspend));
    stopJit(scopedJitSuspend);
    return reinterpret_cast<jlong >(scopedJitSuspend);
}

void epic_startJit(JNIEnv*, jclass, jlong obj) {
    ScopedJitSuspend *scopedJitSuspend = reinterpret_cast<ScopedJitSuspend *>(obj);
    startJit(scopedJitSuspend);
}

void epic_disableMovingGc(JNIEnv* env, jclass ,jint api) {
    void *heap = ArtHelper::getHeap();
    DisableMovingGc(heap);
}

jboolean epic_munprotect(JNIEnv *env, jclass, jlong addr, jlong len) {
    long pagesize = sysconf(_SC_PAGESIZE);
    unsigned alignment = (unsigned)((unsigned long long)addr % pagesize);
    LOGV("munprotect page size: %d, alignment: %d", pagesize, alignment);

    int i = mprotect((void *) (addr - alignment), (size_t) (alignment + len),
                     PROT_READ | PROT_WRITE | PROT_EXEC);
    if (i == -1) {
        LOGV("mprotect failed: %s (%d)", strerror(errno), errno);
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

jboolean epic_cacheflush(JNIEnv *env, jclass, jlong addr, jlong len) {
#if defined(__arm__)
    int i = cacheflush(addr, addr + len, 0);
    LOGV("arm cacheflush for, %ul", addr);
    if (i == -1) {
        LOGV("cache flush failed: %s (%d)", strerror(errno), errno);
        return JNI_FALSE;
    }
#elif defined(__aarch64__)
    char* begin = reinterpret_cast<char*>(addr);
    __builtin___clear_cache(begin, begin + len);
    LOGV("aarch64 __builtin___clear_cache, %p", (void*)begin);
#endif
    return JNI_TRUE;
}

void epic_memcpy(JNIEnv *env, jclass, jlong src, jlong dest, jint length) {
    char *srcPnt = (char *) src;
    char *destPnt = (char *) dest;
    for (int i = 0; i < length; ++i) {
        destPnt[i] = srcPnt[i];
    }
}

void epic_memput(JNIEnv *env, jclass, jbyteArray src, jlong dest) {

    jbyte *srcPnt = env->GetByteArrayElements(src, 0);
    jsize length = env->GetArrayLength(src);
    unsigned char *destPnt = (unsigned char *) dest;
    for (int i = 0; i < length; ++i) {
        // LOGV("put %d with %d", i, *(srcPnt + i));
        destPnt[i] = (unsigned char) srcPnt[i];
    }
    env->ReleaseByteArrayElements(src, srcPnt, 0);
}

jbyteArray epic_memget(JNIEnv *env, jclass, jlong src, jint length) {

    jbyteArray dest = env->NewByteArray(length);
    if (dest == NULL) {
        return NULL;
    }
    unsigned char *destPnt = (unsigned char *) env->GetByteArrayElements(dest, 0);
    unsigned char *srcPnt = (unsigned char *) src;
    for (int i = 0; i < length; ++i) {
        destPnt[i] = srcPnt[i];
    }
    env->ReleaseByteArrayElements(dest, (jbyte *) destPnt, 0);

    return dest;
}

jlong epic_mmap(JNIEnv *env, jclass, jint length) {
    void *space = mmap(0, (size_t) length, PROT_READ | PROT_WRITE | PROT_EXEC,
                       MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
    if (space == MAP_FAILED) {
        LOGV("mmap failed: %d", errno);
        return 0;
    }
    return (jlong) space;
}

void epic_munmap(JNIEnv *env, jclass, jlong addr, jint length) {
    int r = munmap((void *) addr, (size_t) length);
    if (r == -1) {
        LOGV("munmap failed: %d", errno);
    }
}

jlong epic_malloc(JNIEnv *env, jclass, jint size) {
    size_t length = sizeof(void *) * size;
    void *ptr = malloc(length);
    LOGV("malloc :%d of memory at: %p", (int) length, ptr);
    return (jlong) ptr;
}


jobject epic_getobject(JNIEnv *env, jclass clazz, jlong self, jlong address) {
    JavaVM *vm;
    env->GetJavaVM(&vm);
    LOGV("java vm: %p, self: %p, address: %p", vm, (void*) self, (void*) address);
    jobject object = addWeakGloablReference(vm, (void *) self, (void *) address);

    return object;
}

jlong epic_getMethodAddress(JNIEnv *env, jclass clazz, jobject method) {
    jlong art_method = (jlong) env->FromReflectedMethod(method);
    if (art_method % 2 == 1) {
        art_method = reinterpret_cast<jlong>(JniIdManager_DecodeMethodId_(ArtHelper::getJniIdManager(), art_method));
    }
    return art_method;
}

jboolean epic_isGetObjectAvaliable(JNIEnv *, jclass) {
    return (jboolean) (addWeakGloablReference != nullptr);
}

jboolean epic_activate(JNIEnv* env, jclass jclazz, jlong jumpToAddress, jlong pc, jlong sizeOfDirectJump,
                       jlong sizeOfBridgeJump, jbyteArray code) {

    // fetch the array, we can not call this when thread suspend(may lead deadlock)
    jbyte *srcPnt = env->GetByteArrayElements(code, 0);
    jsize length = env->GetArrayLength(code);

    jlong cookie = 0;
    bool isNougat = api_level >= 24;
    if (isNougat) {
        // We do thus things:
        // 1. modify the code mprotect
        // 2. modify the code

        // Ideal, this two operation must be atomic. Below N, this is safe, because no one
        // modify the code except ourselves;
        // But in Android N, When the jit is working, between our step 1 and step 2,
        // if we modity the mprotect of the code, and planning to write the code,
        // the jit thread may modify the mprotect of the code meanwhile
        // we must suspend all thread to ensure the atomic operation.

        LOGV("suspend all thread.");
        cookie = epic_suspendAll(env, jclazz);
    }

    jboolean result = epic_munprotect(env, jclazz, jumpToAddress, sizeOfDirectJump);
    if (result) {
        unsigned char *destPnt = (unsigned char *) jumpToAddress;
        for (int i = 0; i < length; ++i) {
            destPnt[i] = (unsigned char) srcPnt[i];
        }
        jboolean ret = epic_cacheflush(env, jclazz, pc, sizeOfBridgeJump);
        if (!ret) {
            LOGV("cache flush failed!!");
        }
    } else {
        LOGV("Writing hook failed: Unable to unprotect memory at %d", jumpToAddress);
    }

    if (cookie != 0) {
        LOGV("resume all thread.");
        epic_resumeAll(env, jclazz, cookie);
    }

    env->ReleaseByteArrayElements(code, srcPnt, 0);
    return result;
}

static JNINativeMethod dexposedMethods[] = {

        {"mmap",              "(I)J",                          (void *) epic_mmap},
        {"munmap",            "(JI)Z",                         (void *) epic_munmap},
        {"memcpy",            "(JJI)V",                        (void *) epic_memcpy},
        {"memput",            "([BJ)V",                        (void *) epic_memput},
        {"memget",            "(JI)[B",                        (void *) epic_memget},
        {"munprotect",        "(JJ)Z",                         (void *) epic_munprotect},
        {"getMethodAddress",  "(Ljava/lang/reflect/Member;)J", (void *) epic_getMethodAddress},
        {"cacheflush",        "(JJ)Z",                         (void *) epic_cacheflush},
        {"malloc",            "(I)J",                          (void *) epic_malloc},
        {"getObjectNative",   "(JJ)Ljava/lang/Object;",        (void *) epic_getobject},
        {"compileMethod",     "(Ljava/lang/reflect/Member;J)Z",(void *) epic_compile},
        {"suspendAll",        "()J",                           (void *) epic_suspendAll},
        {"resumeAll",         "(J)V",                          (void *) epic_resumeAll},
        {"stopJit",           "()J",                           (void *) epic_stopJit},
        {"startJit",          "(J)V",                          (void *) epic_startJit},
        {"disableMovingGc",   "(I)V",                          (void *) epic_disableMovingGc},
        {"activateNative",    "(JJJJ[B)Z",                     (void *) epic_activate},
        {"isGetObjectAvailable", "()Z",                        (void *) epic_isGetObjectAvaliable}
};

static int registerNativeMethods(JNIEnv *env, const char *className,
                                 JNINativeMethod *gMethods, int numMethods) {

    jclass clazz = env->FindClass(className);
    if (clazz == NULL) {
        return JNI_FALSE;
    }

    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {

    JNIEnv *env = NULL;

    LOGV("JNI_OnLoad");

    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }

    if (!registerNativeMethods(env, JNIHOOK_CLASS, dexposedMethods,
                               sizeof(dexposedMethods) / sizeof(dexposedMethods[0]))) {
        return -1;
    }

    init_entries(env);
    return JNI_VERSION_1_6;
}
