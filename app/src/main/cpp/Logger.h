
#ifndef PUBLISHER_LOGGER_INCLUDE_H
#define PUBLISHER_LOGGER_INCLUDE_H

#include <android/log.h>

#ifndef NDK_LOG_TAG
#define NDK_LOG_TAG "ndk-publish"
#endif


#define ALOGD(...) __android_log_print(ANDROID_LOG_DEBUG, NDK_LOG_TAG, __VA_ARGS__)
#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO,  NDK_LOG_TAG, __VA_ARGS__)
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, NDK_LOG_TAG, __VA_ARGS__)


#endif //PUBLISHER_LOGGER_INCLUDE_H

