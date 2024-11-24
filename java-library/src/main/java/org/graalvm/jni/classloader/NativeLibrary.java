package org.graalvm.jni.classloader;

import java.nio.file.Path;

public final class NativeLibrary {

    private NativeLibrary() {
    }

    public static void init() {
        init0();
    }

    static native void init0();
}