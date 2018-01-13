#! /usr/bin/env ruby

require 'erb'

template = %q{
   /*
    *
    * Unless required by applicable law or agreed to in writing, software
    * distributed under the License is distributed on an "AS IS" BASIS,
    * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    * See the License for the specific language governing permissions and
    * limitations under the License.
    */
   
    package <%=package_name%>;
   
    import android.util.Pair;
   
    import com.taobao.android.dexposed.DexposedBridge;
    import com.taobao.android.dexposed.XposedHelpers;
    import com.taobao.android.dexposed.utility.Logger;
   
    import java.lang.reflect.Method;
    import java.util.concurrent.atomic.AtomicInteger;
    import java.nio.ByteBuffer;
    import java.nio.ByteOrder;
    import java.util.Arrays;
    import java.util.HashMap;
    import java.util.Map;
    import java.util.concurrent.ConcurrentHashMap;
    
    import me.weishu.epic.art.Epic;
    import me.weishu.epic.art.EpicNative;
   
    @SuppressWarnings({"unused", "ConstantConditions"})
    public class <%=class_name%> {

    private static final int MAX_ENTRY = <%=max_index%>;

    // types = ['void', 'boolean', 'byte', 'short', 'char', 'int', 'long', 'float', 'double', 'Object']
    enum TYPE {
        VOID,
        BOOLEAN,
        BYTE,
        SHORT,
        CHAR,
        INT,
        LONG,
        FLOAT,
        DOUBLE,
        OBJECT
    }

    static class Info {
        Class<?> clazz;
        TYPE type;
        AtomicInteger index = new AtomicInteger(0);

        Info(Class<?> clazz, TYPE type) {
            this.clazz = clazz;
            this.type = type;
        }
    }

    private static final Map<Pair<TYPE, Integer>, Epic.MethodInfo> sHookMap = new ConcurrentHashMap<>();

    private static final AtomicInteger sIndex = new AtomicInteger(0);

    private final static String TAG = "<%=class_name%>";

    private static Map<Class<?>, String> bridgeMethodMap = new ConcurrentHashMap<>();
    private static Map<Class<?>, Info> sTypeMap = new ConcurrentHashMap<>();
    static {
        sTypeMap.put(boolean.class, new Info(boolean.class, TYPE.BOOLEAN));
        sTypeMap.put(byte.class, new Info(byte.class, TYPE.BYTE));
        sTypeMap.put(char.class, new Info(char.class, TYPE.CHAR));
        sTypeMap.put(short.class, new Info(short.class, TYPE.SHORT));
        sTypeMap.put(int.class, new Info(int.class, TYPE.INT));
        sTypeMap.put(long.class, new Info(long.class, TYPE.LONG));
        sTypeMap.put(float.class, new Info(float.class, TYPE.FLOAT));
        sTypeMap.put(double.class, new Info(double.class, TYPE.DOUBLE));
        sTypeMap.put(void.class, new Info(void.class, TYPE.VOID));
        sTypeMap.put(Object.class, new Info(Object.class, TYPE.OBJECT));

        Class<?>[] primitiveTypes = new Class[]{boolean.class, byte.class, char.class, short.class,
                int.class, long.class, float.class, double.class};
        for (Class<?> primitiveType : primitiveTypes) {
            bridgeMethodMap.put(primitiveType, primitiveType.getName() + "Bridge");
        }
        bridgeMethodMap.put(Object.class, "ObjectBridge");
        bridgeMethodMap.put(void.class, "voidBridge");
    }

    public static Method getBridgeMethod(Epic.MethodInfo methodInfo) {
        try {
            Class<?> returnType = methodInfo.returnType;
            Class<?> returnTypeToRecognize = returnType.isPrimitive() ? returnType : Object.class;

            Info info = sTypeMap.get(returnTypeToRecognize);
            final int currentIndex = info.index.getAndIncrement();

            if (currentIndex > MAX_ENTRY) {
                throw new RuntimeException("only can hook " + MAX_ENTRY + " methods!!");
            }
            
            synchronized (sHookMap) {
                Pair<TYPE, Integer> indexTypePair = Pair.create(info.type, currentIndex);
                sHookMap.put(indexTypePair, methodInfo);
            }

            int paramNumber = methodInfo.isStatic ? methodInfo.paramNumber : methodInfo.paramNumber + 1;
            Class<?>[] bridgeParamTypes;
            bridgeParamTypes = new Class[paramNumber];
            for (int i = 0; i < paramNumber; i++) {
                bridgeParamTypes[i] = int.class;
            }

            final String bridgeMethod = bridgeMethodMap.get(methodInfo.returnType.isPrimitive() ? returnType : Object.class) + currentIndex;
            Logger.i(TAG, "bridge method:" + bridgeMethod);
            Method method = Entry_3.class.getDeclaredMethod(bridgeMethod, bridgeParamTypes);
            method.setAccessible(true);
            return method;
        } catch (Throwable e) {
            throw new RuntimeException("found bridge method error", e);
        }
    }

    private static int getTypeLength(Class<?> clazz) {
        if (clazz == long.class || clazz == double.class) {
            return 8; // double & long are 8 bytes.
        } else {
            return 4;
        }
    }

    /**
    * construct the method arguments from register r1, r2, r3 and stack
    * @return arguments passed to the callee method
    */
   private static Pair<Object, Object[]> constructArguments(Epic.MethodInfo originMethodInfo, int[] args) {
       boolean isStatic = originMethodInfo.isStatic;

       final long nativePeer = XposedHelpers.getLongField(Thread.currentThread(), "nativePeer");
       final int self = (int) nativePeer;

       Object receiver = null;
       Object[] arguments;

       int numberOfArguments = args.length;
       Class<?>[] paramTypes = originMethodInfo.paramTypes;
       if (isStatic) {
           // receiver is null
           arguments = new Object[numberOfArguments];
           for (int i = 0; i < numberOfArguments; i++) {
               arguments[i] = wrapArgument(paramTypes[i], self, args[i]);
           }

       } else {
           // non-static, r1 = receiver; r2, r3, sp + 16 is arguments.
           receiver = wrapArgument(Object.class, self, args[0]);
           arguments = new Object[numberOfArguments - 1];

           for (int i = 1; i < numberOfArguments; i++) {
               arguments[i - 1] = wrapArgument(paramTypes[i - 1], self, args[i]);
           }
       }

       return Pair.create(receiver, arguments);
   }

   private static Object wrapArgument(Class<?> type, int self, int value) {
       final ByteBuffer byteBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value);
       byteBuffer.flip();
       Logger.d(TAG, "wrapArgument: type:" + type);
       if (type.isPrimitive()) {
           if (type == int.class) {
               return byteBuffer.getInt();
           } else if (type == long.class) {
               return byteBuffer.getLong();
           } else if (type == float.class) {
               return byteBuffer.getFloat();
           } else if (type == short.class) {
               return byteBuffer.getShort();
           } else if (type == byte.class) {
               return byteBuffer.get();
           } else if (type == char.class) {
               return byteBuffer.getChar();
           } else if (type == double.class) {
               return byteBuffer.getDouble();
           } else if (type == boolean.class) {
               return byteBuffer.getInt() == 0;
           } else {
               throw new RuntimeException("unknown type:" + type);
           }
       } else {
           int address = byteBuffer.getInt();
           Object object = EpicNative.getObject(self, address);
           // Logger.i(TAG, "wrapArgument, address: 0x" + Long.toHexString(address) + ", value:" + object);
           return object;
       }
   }
   private static Object wrapArgument(Class<?> type, int self, byte[] value) {
       final ByteBuffer byteBuffer = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN);
       byteBuffer.flip();
       Logger.d(TAG, "wrapArgument: type:" + type);
       if (type.isPrimitive()) {
           if (type == int.class) {
               return byteBuffer.getInt();
           } else if (type == long.class) {
               return byteBuffer.getLong();
           } else if (type == float.class) {
               return byteBuffer.getFloat();
           } else if (type == short.class) {
               return byteBuffer.getShort();
           } else if (type == byte.class) {
               return byteBuffer.get();
           } else if (type == char.class) {
               return byteBuffer.getChar();
           } else if (type == double.class) {
               return byteBuffer.getDouble();
           } else if (type == boolean.class) {
               return byteBuffer.getInt() == 0;
           } else {
               throw new RuntimeException("unknown type:" + type);
           }
       } else {
           int address = byteBuffer.getInt();
           Object object = EpicNative.getObject(self, address);
           // Logger.i(TAG, "wrapArgument, address: 0x" + Long.toHexString(address) + ", value:" + object);
           return object;
       }
   }
   
    private static Object referenceBridge(Epic.MethodInfo originMethodInfo, int... args) {
        Logger.i(TAG, "enter bridge function.");

        final Pair<Object, Object[]> constructArguments = constructArguments(originMethodInfo, args);
        Object receiver = constructArguments.first;
        Object[] arguments = constructArguments.second;

        Logger.i(TAG, "arguments:" + Arrays.toString(arguments));

        Class<?> returnType = originMethodInfo.returnType;
        Object artMethod = originMethodInfo.method;

        if (returnType == void.class) {
            onHookVoid(artMethod, receiver, arguments);
            return 0;
        } else if (returnType == char.class) {
            return onHookCharacter(artMethod, receiver, arguments);
        } else if (returnType == byte.class) {
            return onHookByte(artMethod, receiver, arguments);
        } else if (returnType == short.class) {
            return onHookShort(artMethod, receiver, arguments);
        } else if (returnType == int.class) {
            return onHookInteger(artMethod, receiver, arguments);
        } else if (returnType == long.class) {
            return onHookLong(artMethod, receiver, arguments);
        } else if (returnType == float.class) {
            return onHookFloat(artMethod, receiver, arguments);
        } else if (returnType == double.class) {
            return onHookDouble(artMethod, receiver, arguments);
        } else if (returnType == boolean.class) {
            return onHookBoolean(artMethod, receiver, arguments);
        } else {
            return onHookObject(artMethod, receiver, arguments);
        }
    }


    <%# generate callback %>
    //region ---------------callback---------------<%=-%>
    <%for type in java_types.keys %>
    private static <%=type%> onHook<%=java_types[type]%>(Object artmethod, Object receiver, Object[] args) {
        <%= 'return' if not type.to_s.eql?("void")%> <%= "(" + java_types[type] + ") " if not type.to_s.eql?("void")%>DexposedBridge.handleHookedArtMethod(artmethod, receiver, args);
    }
    <%end%>
    //endregion
    <%for argument_number in max_arguments.times%>
    //region ---------------<%=argument_number%> parameters bridge---------------
    <%for index in max_index.times%>
    <%for type in java_types.keys%>
    <%# generate bridge methods %>
    private static <%=type%> <%=type%>Bridge<%=index%>(<%for arg in argument_number.times%>int arg<%=arg%><%=', ' if arg < argument_number - 1%><%end%>) {
        Pair<TYPE, Integer> typeIndexPair = Pair.create(TYPE.<%=type.upcase%>, <%=index%>);
        Epic.MethodInfo originMethodInfo = sHookMap.get(typeIndexPair);
        <%= 'return' if not type.to_s.eql?("void")%> <%= "(" + java_types[type] + ") " if not type.to_s.eql?("void")%>referenceBridge(originMethodInfo<%=', ' if argument_number > 0%><%for arg in argument_number.times%>arg<%=arg%><%=', ' if arg < argument_number - 1%><%end%>);
    }
    <%end%><%=-%>
    <%end%>
    //endregion
    <%end%>


}
}

java_types = {
    # types = ['void', 'boolean', 'byte', 'short', 'char', 'int', 'long', 'float', 'double', 'Object']
    'void': 'Void',
    'boolean': 'Boolean',
    'byte': 'Byte',
    'short': 'Short',
    'char': 'Character',
    'int': 'Integer',
    'long': 'Long',
    'float': 'Float',
    'double': 'Double',
    'Object': 'Object'
}
max_index = 256
max_arguments = 10
class_name = 'Entry_3'
package_name = 'me.weishu.epic.art.entry'

erb_template = ERB.new(template, nil, '-')
sqlFile = File.new("./#{class_name}.java", "w")  
sqlFile.puts erb_template.result  

