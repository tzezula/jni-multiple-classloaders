package org.graalvm.jni.app;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

public final class Main {

    public static void main(String[] args) throws Exception {
        String nativeLibraryProject = System.getProperty("native.library.project");
        if (nativeLibraryProject == null) {
            throw new IllegalArgumentException( "native.library.project must point to maven native library NAR project.");
        }
        String javaLibraryProject = System.getProperty("java.library.project");
        if (javaLibraryProject == null) {
            throw new IllegalArgumentException( "java.library.project must point to Java library project.");
        }
        Path nativeLibrary = findNativeLibrary(nativeLibraryProject);
        Path javaLibrary = findJavaLibrary(javaLibraryProject);

        System.out.println("Native library loaded by system classloader: "  + nativeLibrary);
        System.out.println("Java library with native method loaded in custom classloaders: " + javaLibrary);

        // Load a native library in the system classloader
        System.load(nativeLibrary.toString());

        // Create a custom classloader with the system classloader as a parent
        ClassLoader classLoader1 = new URLClassLoader(new URL[]{javaLibrary.toUri().toURL()}, Main.class.getClassLoader());
        Class<?> nativeLibraryInClassLoader1 = classLoader1.loadClass("org.graalvm.jni.classloader.NativeLibrary");
        nativeLibraryInClassLoader1.getDeclaredMethod("init").invoke(null);

        // Create a custom classloader with the system classloader as a parent
        ClassLoader classLoader2 = new URLClassLoader(new URL[]{javaLibrary.toUri().toURL()}, Main.class.getClassLoader());
        Class<?> nativeLibraryInClassLoader2 = classLoader2.loadClass("org.graalvm.jni.classloader.NativeLibrary");
        nativeLibraryInClassLoader2.getDeclaredMethod("init").invoke(null);
    }

    private static Path findJavaLibrary(String javaLibraryProject) throws IOException {
        Path target = Path.of(javaLibraryProject, "target");
        if (!Files.isDirectory(target)) {
            throw new IllegalArgumentException( target + " directory does not exist.");
        }
        try (Stream<Path> stream = Files.walk(target)) {
            return stream.filter(Main::isJavaArchive).findAny().orElseThrow(() -> new NoSuchElementException("No shared library found in " + target));
        }
    }

    private static boolean isJavaArchive(Path path) {
        try {
            return path.getFileName().toString().endsWith(".jar") && Files.isRegularFile(path) && Files.size(path) > 0;
        } catch (IOException ioe) {
            return false;
        }
    }

    private static Path findNativeLibrary(String nativeLibraryProject) throws IOException  {
        Path target = Path.of(nativeLibraryProject, "target");
        if (!Files.isDirectory(target)) {
            throw new IllegalArgumentException( target + " directory does not exist.");
        }
        try (Stream<Path> stream = Files.walk(target)) {
            return stream.filter(Main::isSharedLibrary).findAny().orElseThrow(() -> new NoSuchElementException("No shared library found in " + target));
        }
    }

    private static boolean isSharedLibrary(Path path) {
        String name = path.getFileName().toString();
        String os = System.getProperty("os.name");
        if (os.equalsIgnoreCase("linux")) {
            return name.endsWith(".so");
        } else if (os.equalsIgnoreCase("mac os x") || os.equalsIgnoreCase("darwin")) {
            return name.endsWith(".dylib");
        } else {
            throw new UnsupportedOperationException("Not supported os " + os);
        }
    }
}