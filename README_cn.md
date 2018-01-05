## 简介

Epic是一个在虚拟机层面、以Java Method为粒度的 **运行时** AOP Hook框架。简单来说，Epic 就是ART上的 [Dexposed](https://github.com/alibaba/dexposed) 。它可以拦截本进程内部几乎任意的Java方法调用，可用于实现AOP编程、运行时插桩、性能分析、安全审计等。

关于Epic的实现原理，可以参考 [本文](http://weishu.me/2017/11/23/dexposed-on-art/)。

## 使用

### 添加依赖

在你项目的build.gradle 中添加如下依赖（jcenter仓库):

```groovy
dependencies {
    compile 'me.weishu:epic:0.3.3'
}
```

然后就可以使用了。

### 几个例子

1. 监控Java线程的创建和销毁：

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

以上代码拦截了 `Thread` 类以及 `Thread` 类所有子类的 `run`方法，在 `run` 方法开始执行和退出的时候进行拦截，就可以知道进程内部所有Java线程创建和销毁的时机；更进一步，你可以结合Systrace等工具，来生成整个过程的执行流程图，比如：

<img src="http://7xp3xc.com1.z0.glb.clouddn.com/201601/1511840542774.png" width="480"/>

2. 监控dex文件的加载：

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

目前Epic支持 Android 5.0~ 8.1 的 Thumb2/ARM64指令集；Android O的支持正在计划中，x86/mips/arm32的支持后续也会完成。但是，本项目没有经过任何线上产品的验证，无法保证足够的稳定性；目前仅仅是个人用途（主要是性能分析），欢迎给我提 issue :)

Android版本支持情况：

Runtime | Android Version | Support
------  | --------------- | --------
Dalvik  | 2.2             | Not Test
Dalvik  | 2.3             | Yes
Dalvik  | 3.0             | No
Dalvik  | 4.0-4.4         | Yes
ART     | L (5.0)         | Yes
ART     | L MR1 (5.1)     | Yes
ART     | M (6.0)         | Yes
ART     | N (7.0)         | Yes
ART     | N MR1 (7.1)     | Yes
ART     | O (8.0)         | Yes
ART     | O MR1(8.1)      | Yes

指令集支持情况：

Runtime  | Arch         | Support
-------- | ------------ | --------
Dalvik   | All          | Yes
ART      | Thumb2       | Yes
ART      | ARM64        | Yes
ART      | ARM32        | No
ART      | x86/x86_64   | No
ART      | mips         | No

## 已知问题

1. 受限于inline hook本身，短方法 (Thum2下指令小于8个字节，ARM64小于16字节) 无法支持。
2. 被完全内联的方法不支持。
3. 在支持硬浮点的CPU架构(如armeabi-v7a, arm64-v8a)上，参数包含 double/float 的方法支持可能有问题，还没有进行充分地测试。
4. Android L 的 arm64 暂不支持。

## 支持和加入Epic

目前Epic仅仅在 Android 5.0, 5.1, 6.0, 7.0, 7.1, 8.0, 8.1 的个别机型上进行过测试，没有经过大范围的测试，因此很多机型没有覆盖到；欢迎帮助进行兼容性测试，有能力的欢迎贡献代码 :)

你可以拿出你手头的手机，然后clone本项目到本地，然后build其中的 app 模块，安装这个测试APP到你的手机上，点击一下其中的按钮，如果提示有 「测试不通过」，或者有直接闪退的情况，请把Issue砸向我，不胜感激 ^_^ 

## 致谢

1. [Dexposed](https://github.com/alibaba/dexposed)
2. [Xposed](http://repo.xposed.info/module/de.robv.android.xposed.installer)
3. [mar-v-in/ArtHook](https://github.com/mar-v-in/ArtHook)
4. [Nougat_dlfunctions](https://github.com/avs333/Nougat_dlfunctions.git)


## 交流和讨论

twsxtd@gmail.com

[交流群](https://gitter.im/android-hacker/epic?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) 