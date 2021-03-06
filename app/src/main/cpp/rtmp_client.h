
#ifndef KU_LIVE_RTMP_CLIENT_H_INCLUDE
#define KU_LIVE_RTMP_CLIENT_H_INCLUDE

#include "Logger.h"
#include "srs_librtmp.h"


#ifdef NDK_LOG_TAG
    #undef  NDK_LOG_TAG
    #define NDK_LOG_TAG "NDK_RTMP_CLIENT"
#endif


class RtmpClient_SRS {
private:
    srs_rtmp_t rtmp;

public:
    explicit RtmpClient_SRS(const char *url_);

    ~RtmpClient_SRS();

    void connect(bool publish);

    int audioRawFrameWrite(
        char format, char rate, char size, char type,
        char *frame, int frame_size,
        u_int32_t timestamp);

    int videoH264RawFramesWrite(char *frames, int frames_size, u_int32_t dts, u_int32_t pts);

    int writePacket(int type, uint32_t timestamp, char *data, int size);
};



#include "librtmp/rtmp.h"

class RtmpClient {
public:
    struct VideoPackage
    {
        int8_t  *sps;
        int16_t  sps_length;

        int8_t  *pps;
        int16_t  pps_length;
    };


private:
    RTMP         *rtmp     = nullptr;
    VideoPackage *vPackage = nullptr;

    void preMakeVideo(int8_t *buffer, int length, VideoPackage *package);

    RTMPPacket *makeVideoPackage(int8_t *buffer, int length, const long timestamp);

    // make sps pps package
    RTMPPacket *makeVideoPackage(VideoPackage *package);

    void writeVideoPackage(char *data, int size, uint32_t timestamp);
    void writeAudioPackage(char *data, int size, uint32_t timestamp);

public:
    explicit RtmpClient(const char *url_);

    ~RtmpClient();

    void connect(bool publish);

    int audioRawFrameWrite(
        char format, char rate, char size, char type,
        char *frame, int frame_size,
        u_int32_t timestamp);

    int videoH264RawFramesWrite(char *frames, int frames_size, u_int32_t dts, u_int32_t pts);

    int writePacket(int type, uint32_t timestamp, char *data, int size);
};


#endif //KU_LIVE_RTMP_CLIENT_H_INCLUDE

