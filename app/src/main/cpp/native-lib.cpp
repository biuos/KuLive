#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_dai_android_media_live_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}