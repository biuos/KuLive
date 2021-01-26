
#include <jni.h>
#include <cstring>

#include "librtmp/rtmp.h"
#include "Logger.h"


extern "C"
JNIEXPORT jlong JNICALL
Java_dai_android_media_live_rtmp_RtmpClient_open(
    JNIEnv *env, jclass clazz, jstring url, jboolean is_publish_mode) {
    const char *url_c = env->GetStringUTFChars(url, nullptr);
    ALOGI("RtmpClient_open: %s", url_c);

    int   ret  = 0;
    RTMP *rtmp = RTMP_Alloc();
    if (nullptr == rtmp) {
        ALOGE("RTMP_Alloc failed");
        goto exit_label;
    }

    RTMP_Init(rtmp);
    ret = RTMP_SetupURL(rtmp, const_cast<char *>( url_c));
    if (ret != TRUE) {
        ALOGE("RTMP_SetupURL failed");
        RTMP_Free(rtmp);
        rtmp = nullptr;

        goto exit_label;
    }
    ALOGD("RtmpClient_open: setup rtmp url success");

    if (is_publish_mode) {
        RTMP_EnableWrite(rtmp);
    }

    ret = RTMP_Connect(rtmp, nullptr);
    if (ret != TRUE) {
        ALOGE("RTMP_Connect failed");
        RTMP_Free(rtmp);
        rtmp = nullptr;

        goto exit_label;
    }
    ALOGD("RtmpClient_open: connect rtmp server success");

    ret = RTMP_ConnectStream(rtmp, 0);
    if (ret != TRUE) {
        ALOGE("RTMP_ConnectStream failed");
        RTMP_Close(rtmp);
        RTMP_Free(rtmp);
        rtmp = nullptr;

        goto exit_label;
    }
    ALOGD("RtmpClient_open: connect stream success");

exit_label:
    env->ReleaseStringUTFChars(url, url_c);
    return (jlong) rtmp;
}


extern "C"
JNIEXPORT jint JNICALL
Java_dai_android_media_live_rtmp_RtmpClient_read(
    JNIEnv *env, jclass clazz, jlong handler, jbyteArray data_, jint offset, jint size) {
    ALOGI("RtmpClient_read: handler=%ld  offset=%d  size=%d", handler, offset, size);
    auto *rtmp       = reinterpret_cast<RTMP *>(handler);
    char *data       = nullptr;
    int   read_count = 0;
    if (nullptr == rtmp) {
        ALOGE("RtmpClient_read: null object of RTMP");
        goto exit_label;
    }

    data       = new char[size * sizeof(char)];
    read_count = RTMP_Read(rtmp, data, size);
    if (read_count > 0) {
        env->SetByteArrayRegion(data_, offset, read_count, reinterpret_cast<const jbyte *>(data));
    }

exit_label:
    if (data) {
        delete[] data;
        data = nullptr;
    }
    return read_count;
}

extern "C"
JNIEXPORT jint JNICALL
Java_dai_android_media_live_rtmp_RtmpClient_write(
    JNIEnv *env, jclass clazz, jlong handler, jbyteArray data, jint size, jint type, jint ts) {
    ALOGI("RtmpClient_write: handler=%ld  size=%d  type=%d  ts=%d", handler, size, type, ts);
    auto       *rtmp   = reinterpret_cast<RTMP *>(handler);
    jbyte      *buffer = nullptr;
    RTMPPacket *packet = nullptr;
    int         ret    = 0;
    if (nullptr == rtmp) {
        ALOGE("RtmpClient_write: null object of RTMP");
        goto exit_label;
    }

    buffer = env->GetByteArrayElements(data, nullptr);
    packet = new RTMPPacket;
    RTMPPacket_Alloc(packet, size);
    RTMPPacket_Reset(packet);
    if (type == RTMP_PACKET_TYPE_INFO)        // metadata
        packet->m_nChannel = 0x03;
    else if (type == RTMP_PACKET_TYPE_VIDEO)  // video
        packet->m_nChannel = 0x04;
    else if (type == RTMP_PACKET_TYPE_AUDIO)  // audio
        packet->m_nChannel = 0x05;
    else
        packet->m_nChannel = -1;

    packet->m_nInfoField2     = rtmp->m_stream_id;
    std::memcpy(packet->m_body, buffer, size);
    packet->m_headerType      = RTMP_PACKET_SIZE_LARGE;
    packet->m_hasAbsTimestamp = FALSE;
    packet->m_nTimeStamp      = ts;
    packet->m_packetType      = type;
    packet->m_nBodySize       = size;

    ret = RTMP_SendPacket(rtmp, packet, 0);
    if (ret != TRUE) {
        ALOGE("RtmpClient_write: send rtmp packet failed");
    }
    RTMPPacket_Free(packet);
    delete packet;
    packet = nullptr;

exit_label:
    env->ReleaseByteArrayElements(data, buffer, 0);

    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_dai_android_media_live_rtmp_RtmpClient_close(JNIEnv *env, jclass clazz, jlong handler) {
    ALOGI("RtmpClient_close: handler=%ld", handler);
    auto *rtmp = reinterpret_cast<RTMP *>(handler);
    if (!rtmp) {
        ALOGE("RtmpClient_close: null object of RTMP");
        return -1;
    }

    RTMP_Close(rtmp);
    RTMP_Free(rtmp);

    return 0;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_dai_android_media_live_rtmp_RtmpClient_getIpAddr(JNIEnv *env, jclass clazz, jlong handler) {
    ALOGI("RtmpClient_getIpAddr: handler=%ld", handler);
    auto *rtmp = reinterpret_cast<RTMP *>(handler);
    if (rtmp) {
        return env->NewStringUTF(rtmp->Link.hostname.av_val);
    }

    return env->NewStringUTF("");
}
