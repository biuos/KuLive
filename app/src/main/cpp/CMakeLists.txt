cmake_minimum_required(VERSION 3.10.2)

project("publisher")

add_definitions(-DNO_CRYPTO)

add_library(
        publisher SHARED

        # add librtmp source codes and head file
        librtmp/amf.h
        librtmp/amf.c
        librtmp/dh.h
        librtmp/dhgroups.h
        librtmp/handshake.h
        librtmp/hashswf.c
        librtmp/http.h
        librtmp/log.h
        librtmp/log.c
        librtmp/parseurl.c
        librtmp/rtmp.h
        librtmp/rtmp.c
        librtmp/rtmp_sys.h

        RtmpClient_jni.cpp
        native-lib.cpp)


target_link_libraries(
        publisher

        # included in the NDK.
        log)