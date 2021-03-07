cmake_minimum_required(VERSION 3.10.2)

ADD_DEFINITIONS(-DNO_CRYPTO)

add_library( rtmp_librtmp STATIC
        amf.c
        hashswf.c
        http.h
        log.c
        parseurl.c
        rtmp.c
)
