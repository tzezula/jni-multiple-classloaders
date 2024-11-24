# jni-multiple-classloaders-test
Tests loading of a JNI native library with multiple classloaders.

The test application is composed of three modules:

1. `native-library`: A C++ project that builds a simple shared library implementing the native method `org.graalvm.jni.classloader.NativeLibrary#init0`.
2. `java-library`: A Java project that provides the  `org.graalvm.jni.classloader.NativeLibrary` class.
3. `app`: A Java main project that loads a native library using the system class loader and creates two `URLClassLoader` instances, each with the system class loader as their parent. These `URLClassLoader` instances are configured to load the `java-library` artifacts.

## Usage
```
jni-mulitple-classloaders> mvn install
jni-mulitple-classloaders> cd app
app> mvn exec:exec
```

## Output
```
...
Exception in thread "main" java.lang.reflect.InvocationTargetException
        at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:118)
        at java.base/java.lang.reflect.Method.invoke(Method.java:580)
        at app@1.0-SNAPSHOT/org.graalvm.jni.app.Main.main(Main.java:35)
Caused by: java.lang.UnsatisfiedLinkError: 'void org.graalvm.jni.classloader.NativeLibrary.init0()'
        at org.graalvm.jni.classloader.NativeLibrary.init0(Native Method)
        at org.graalvm.jni.classloader.NativeLibrary.init(NativeLibrary.java:11)
        at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103)
        ... 2 more
[ERROR] Command execution failed.
...
```
## Explanation
When HotSpot looks up a native library for a method, it consults the `static long ClassLoader#findNative(ClassLoader loader, Class<?> clazz, String entryName, String javaName)` method.
This method is invoked with the class loader that loaded the enclosing class of the native method. The `findNative` method exclusively uses 
the `NativeLibraries` instance of the provided class loader and does not perform parent class loader delegation. As a result,
the native library must be loaded by the same class loader that loads the class containing the native method.
