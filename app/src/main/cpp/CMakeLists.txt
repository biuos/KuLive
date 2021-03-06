cmake_minimum_required(VERSION 3.10.2)

project("KuLive")


include_directories(
        ${CMAKE_CURRENT_SOURCE_DIR}
        ${CMAKE_CURRENT_SOURCE_DIR}/srs-librtmp
)

ADD_DEFINITIONS(-DNO_CRYPTO)
SET(LIBRTMP_SRC
        librtmp/amf.c
        librtmp/hashswf.c
        librtmp/http.h
        librtmp/log.c
        librtmp/parseurl.c
        librtmp/rtmp.c)


add_library(
        KuLive SHARED

        ${LIBRTMP_SRC}
        srs-librtmp/srs_librtmp.cpp
        android_main.cpp
        rtmp_client.cpp
        rtmp_client_jni.cpp
)


target_link_libraries(
        KuLive

        # included in the NDK.
        log)