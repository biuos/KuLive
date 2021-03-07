#include <jni.h>
#include <cstring>
#include <string>

#include "Logger.h"
#include "IRtmpClient.h"


extern "C" JNIEXPORT jlong JNICALL
Java_dai_android_media_live_rtmp_RtmpClient__1nativeConnect(
    JNIEnv *env, jobject thiz, jstring url, jboolean publish)
{
    ALOGI(">>>> _nativeConnect <<<<");

    const char *url_c = env->GetStringUTFChars(url, nullptr);
    auto *client = new RtmpClient;
    client->init(url_c);
    env->ReleaseStringUTFChars(url, url_c);
    return (jlong) client;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_dai_android_media_live_rtmp_RtmpClient__1aac_1spec_1send(
    JNIEnv *env, jobject thiz,
    jlong ptr, jbyteArray data, jint length)
{
    ALOGI(">>>> _aac_spec_send <<<<");

    int result = 0;
    auto *rtmp = reinterpret_cast<IRtmpClient *>(ptr);

    jbyte *data_ = env->GetByteArrayElements(data, nullptr);
    result = rtmp->write_aac_spec((uint8_t *) data, length);
    env->ReleaseByteArrayElements(data, data_, 0);
    return result == 0;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_dai_android_media_live_rtmp_RtmpClient__1aac_1data_1send(
    JNIEnv *env, jobject thiz, jlong ptr,
    jbyteArray data, jint length, jlong timestamp)
{
    ALOGI(">>>> _aac_data_send <<<<");

    int result = 0;
    auto *rtmp = reinterpret_cast<IRtmpClient *>(ptr);

    jbyte *data_ = env->GetByteArrayElements(data, nullptr);
    result = rtmp->write_aac_data((uint8_t *) data, length, timestamp);
    env->ReleaseByteArrayElements(data, data_, 0);
    return result == 0;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_dai_android_media_live_rtmp_RtmpClient__1sps_1pps_1send(
    JNIEnv *env, jobject thiz, jlong ptr,
    jbyteArray sps_, jint sps_length,
    jbyteArray pps_, jint pps_length,
    jlong timestamp)
{
    ALOGI(">>>> _sps_pps_send <<<<");

    auto *rtmp = reinterpret_cast<IRtmpClient *>(ptr);

    int result = 0;
    jbyte *sps = env->GetByteArrayElements(sps_, nullptr);
    jbyte *pps = env->GetByteArrayElements(pps_, nullptr);

    result = rtmp->write_sps_pps((uint8_t *) sps, sps_length,
                                 (uint8_t *) pps, pps_length,
                                 timestamp);

    env->ReleaseByteArrayElements(sps_, sps, 0);
    env->ReleaseByteArrayElements(pps_, pps, 0);

    return result == 0;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_dai_android_media_live_rtmp_RtmpClient__1h264_1data_1send(
    JNIEnv *env, jobject thiz, jlong ptr,
    jbyteArray data_, jint length, jlong timestamp)
{
    ALOGI(">>>> _h264_data_send <<<<");

    auto *rtmp = reinterpret_cast<IRtmpClient *>(ptr);
    int result = 0;

    jbyte *data = env->GetByteArrayElements(data_, nullptr);
    rtmp->write_video_data((uint8_t *) data, length, timestamp);
    env->ReleaseByteArrayElements(data_, data, 0);

    return result == 0;
}

extern "C" JNIEXPORT void JNICALL
Java_dai_android_media_live_rtmp_RtmpClient__1nativeRelease(JNIEnv *env, jobject thiz, jlong ptr)
{
    ALOGI(">>>> _nativeRelease <<<<");

    auto *rtmp = reinterpret_cast<IRtmpClient *>(ptr);
    delete rtmp;
}
