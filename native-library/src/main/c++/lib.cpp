#include<iostream>

#include<jni.h>

void initImpl() {
    using namespace std;
    cout << "Hello" << endl;
}

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_org_graalvm_jni_classloader_NativeLibrary_init0(JNIEnv *env, jclass clz) {
    initImpl();
}

#ifdef __cplusplus
}
#endif