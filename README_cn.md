## 简介

Epic 是一个在虚拟机层面、以 Java Method 为粒度的 **运行时** AOP Hook 框架。简单来说，Epic 就是 ART 上的 [Dexposed](https://github.com/alibaba/dexposed)（支持 Android 5.0 ~ 11）。它可以拦截本进程内部几乎任意的 Java 方法调用，可用于实现 AOP 编程、运行时插桩、性能分析、安全审计等。

Epic 被 [VirtualXposed](https://github.com/android-hacker/VirtualXposed) 以及 [太极](https://www.coolapk.com/apk/me.weishu.exp) 使用，用来实现非 Root 场景下的 Xposed 功能，已经经过了相当广泛的验证。

关于 Epic 的实现原理，可以参考 [本文](http://weishu.me/2017/11/23/dexposed-on-art/)。

## 使用

### 添加依赖

在你项目的 build.gradle 中添加如下依赖（jitpack 仓库):

```groovy
dependencies {
    compile 'com.github.tiann:epic:0.11.2'
}
```

然后就可以使用了。


### 几个例子

1. 监控 Java 线程的创建和销毁：

```java
class ThreadMethodHook extends XC_MethodHook{
    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        super.beforeHookedMethod(param);
        Thread t = (Thread) param.thisObject;
        Log.i(TAG, "thread:" + t + ", started..");
    }

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        super.afterHookedMethod(param);
        Thread t = (Thread) param.thisObject;
        Log.i(TAG, "thread:" + t + ", exit..");
    }
}

DexposedBridge.hookAllConstructors(Thread.class, new XC_MethodHook() {
    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        super.afterHookedMethod(param);
        Thread thread = (Thread) param.thisObject;
        Class<?> clazz = thread.getClass();
        if (clazz != Thread.class) {
            Log.d(TAG, "found class extend Thread:" + clazz);
            DexposedBridge.findAndHookMethod(clazz, "run", new ThreadMethodHook());
        }
        Log.d(TAG, "Thread: " + thread.getName() + " class:" + thread.getClass() +  " is created.");
    }
});
DexposedBridge.findAndHookMethod(Thread.class, "run", new ThreadMethodHook());
```

以上代码拦截了 `Thread` 类以及 `Thread` 类所有子类的 `run`方法，在 `run` 方法开始执行和退出的时候进行拦截，就可以知道进程内部所有 Java 线程创建和销毁的时机；更进一步，你可以结合 Systrace 等工具，来生成整个过程的执行流程图，比如：

<img src="http://7xp3xc.com1.z0.glb.clouddn.com/201601/1511840542774.png" width="480"/>

2. 监控 dex 文件的加载：

```java
DexposedBridge.findAndHookMethod(DexFile.class, "loadDex", String.class, String.class, int.class, new XC_MethodHook() {
    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        super.beforeHookedMethod(param);
        String dex = (String) param.args[0];
        String odex = (String) param.args[1];
        Log.i(TAG, "load dex, input:" + dex + ", output:" + odex);
    }
});
```

## 支持情况

目前 Epic 支持 Android 5.0 ~ 11 的 Thumb-2/ARM64 指令集，arm32/x86/x86_64/mips/mips64 不支持。本项目被 [VirtualXposed](https://github.com/android-hacker/VirtualXposed) 和 [太极](http://taichi.cool) 以及大量企业级用户使用，经过了数千万用户的验证，已经被证明非常稳定。目前，手机 QQ 已经在产品中使用 Epic。


## 已知问题

1. 受限于 inline hook 本身，短方法 (Thumb-2 下指令小于 8 个字节，ARM64 小于 16 字节) 无法支持。
2. 被完全内联的方法不支持。

## 致谢

1. [Dexposed](https://github.com/alibaba/dexposed)
2. [Xposed](http://repo.xposed.info/module/de.robv.android.xposed.installer)
3. [mar-v-in/ArtHook](https://github.com/mar-v-in/ArtHook)
4. [Nougat_dlfunctions](https://github.com/avs333/Nougat_dlfunctions.git)


## 联系我

twsxtd@gmail.com
