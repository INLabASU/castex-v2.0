#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring

JNICALL
Java_info_jkjensen_castexv2_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from the underworld";
    return env->NewStringUTF(hello.c_str());
}
