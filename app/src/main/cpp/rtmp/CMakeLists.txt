cmake_minimum_required(VERSION 3.10.2)

ADD_SUBDIRECTORY(${CMAKE_CURRENT_SOURCE_DIR}/librtmp)
ADD_SUBDIRECTORY(${CMAKE_CURRENT_SOURCE_DIR}/srs-librtmp)

INCLUDE_DIRECTORIES(${CMAKE_CURRENT_SOURCE_DIR}/../log)

add_library(
        ku_live_rtmp STATIC

        impl_rtmp.cpp
        impl_srs_rtmp.cpp
)


target_link_libraries(
        ku_live_rtmp

        # linke rtmp
        rtmp_librtmp rtmp_srs_librtmp

        # included in the NDK.
        log)