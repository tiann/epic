package me.weishu.epic.samples.tests.custom;

import android.os.SystemClock;
import android.util.Log;

import com.taobao.android.dexposed.utility.Debug;
import com.taobao.android.dexposed.utility.Unsafe;

import java.lang.reflect.Method;

import de.robv.android.xposed.XposedHelpers;
import me.weishu.epic.art.EpicNative;
import me.weishu.epic.art.method.ArtMethod;

/**
 * Created by weishu on 17/11/6.
 */

public class Case6 implements Case {

    private static final String TAG = "Case6";

    @Override
    public void hook() {
        Object test = new Object();
        Log.i(TAG, "test object:" + test);

        long testAddr = Unsafe.getObjectAddress(test);
        Log.i(TAG, "test object address :" + testAddr);
        Log.i(TAG, "test object :" + EpicNative.getObject(XposedHelpers.getLongField(Thread.currentThread(), "nativePeer"), testAddr));

        // Log.i(TAG, "object:" + EpicNative.getObject())
        final Method nanoTime = XposedHelpers.findMethodExact(System.class, "nanoTime");
        final Method uptimeMillis = XposedHelpers.findMethodExact(SystemClock.class, "uptimeMillis");
        final Method map = XposedHelpers.findMethodExact(Target.class, "test1", Object.class, int.class);
        final Method malloc = XposedHelpers.findMethodExact(Target.class, "test3", Object.class, int.class);

        ArtMethod artMethod1 = ArtMethod.of(nanoTime);
        ArtMethod artMethod2 = ArtMethod.of(uptimeMillis);

        ArtMethod artMethod3 = ArtMethod.of(map);
        ArtMethod artMethod4 = ArtMethod.of(malloc);

        Log.i(TAG, "nanoTime: addr: 0x" + artMethod1.getAddress() + ", entry:" + Debug.addrHex(artMethod1.getEntryPointFromQuickCompiledCode()));
        Log.i(TAG, "uptimeMills: addr: 0x" + artMethod2.getAddress() + ", entry:" + Debug.addrHex(artMethod2.getEntryPointFromQuickCompiledCode()));
        Log.i(TAG, "map : addr: 0x" + artMethod3.getAddress() + ", entry:" + Debug.addrHex(artMethod3.getEntryPointFromQuickCompiledCode()));
        Log.i(TAG, "malloc: addr: 0x" + artMethod4.getAddress() + ", entry:" + Debug.addrHex(artMethod4.getEntryPointFromQuickCompiledCode()));
    }

    @Override
    public boolean validate(Object... args) {
        return true;
    }
}
