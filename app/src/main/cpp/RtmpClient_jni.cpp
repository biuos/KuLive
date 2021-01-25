
#include <jni.h>


extern "C"
JNIEXPORT jlong JNICALL
Java_dai_android_media_live_rtmp_RtmpClient_open(
        JNIEnv *env, jclass clazz, jstring url, jboolean is_publish_mode) {
    // TODO: implement open()
}


extern "C"
JNIEXPORT jint JNICALL
Java_dai_android_media_live_rtmp_RtmpClient_read(
        JNIEnv *env, jclass clazz,
        jlong handler, jbyteArray data, jint offset, jint size) {
    // TODO: implement read()
}

extern "C"
JNIEXPORT jint JNICALL
Java_dai_android_media_live_rtmp_RtmpClient_write(
        JNIEnv *env, jclass clazz,
        jlong handler, jbyteArray data, jint size, jint type, jint ts) {
    // TODO: implement write()
}

extern "C"
JNIEXPORT jint JNICALL
Java_dai_android_media_live_rtmp_RtmpClient_close(JNIEnv *env, jclass clazz, jlong handler) {
    // TODO: implement close()
}

extern "C"
JNIEXPORT jstring JNICALL
Java_dai_android_media_live_rtmp_RtmpClient_getIpAddr(JNIEnv *env, jclass clazz, jlong handler) {
    // TODO: implement getIpAddr()
}
