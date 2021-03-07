cmake_minimum_required(VERSION 3.10.2)

project("KuLive")


ADD_SUBDIRECTORY(${CMAKE_CURRENT_SOURCE_DIR}/rtmp)

include_directories(
        ${CMAKE_CURRENT_SOURCE_DIR}/rtmp
        ${CMAKE_CURRENT_SOURCE_DIR}/log
)


add_library(
        KuLive SHARED

        android_main.cpp
        rtmp_client_jni.cpp
)


target_link_libraries(
        KuLive

        # linke rtmp
        ku_live_rtmp

        # included in the NDK.
        log)