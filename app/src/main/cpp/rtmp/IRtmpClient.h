
#ifndef KU_LIVE_RTMP_CLIENT_H_INCLUDE
#define KU_LIVE_RTMP_CLIENT_H_INCLUDE


#include "librtmp/rtmp.h"
#include "srs-librtmp/srs_librtmp.h"

#include <cstdint>

class IRtmpClient {
public:
    virtual int init(const char *url) = 0;

    // 发送 SPS  PPS
    virtual int
    write_sps_pps(uint8_t *sps, int spsLength, uint8_t *pps, int ppsLength, long timestamp) = 0;

    // 发送视频数据
    virtual int write_video_data(uint8_t *data, int length, long timestamp) = 0;

    virtual int write_aac_spec(uint8_t *data, int length) = 0;

    virtual int write_aac_data(uint8_t *data, int length, long timestamp) = 0;

    virtual int stop() = 0;

    virtual ~IRtmpClient() = default;
};


//
// librtmp client implement
//
class RtmpClient : public IRtmpClient {
private:
    RTMP    *rtmp;

    uint8_t *sps;
    int      spsLength;
    uint8_t *pps;
    int      ppsLength;

    void free_sps_pps();
    bool do_sps_pps(const uint8_t *buffer, int length);

public:
    virtual int init(const char *url) override;

    virtual int write_sps_pps(uint8_t *sps, int spsLength,
                              uint8_t *pps, int ppsLength,
                              long timestamp) override;

    virtual int write_video_data(uint8_t *data, int length, long timestamp) override;

    virtual int write_aac_spec(uint8_t *data, int length) override;

    virtual int write_aac_data(uint8_t *data, int length, long timestamp) override;

    virtual int stop() override;

    virtual ~RtmpClient();
};


//
// srs-librtmp client implement
//
class SRSRtmpClient : public IRtmpClient {
public:
    virtual int init(const char *url) override;

    virtual int write_sps_pps(uint8_t *sps, int spsLength,
                              uint8_t *pps, int ppsLength,
                              long timestamp) override;

    virtual int write_video_data(uint8_t *data, int length, long timestamp) override;

    virtual int write_aac_spec(uint8_t *data, int length) override;

    virtual int write_aac_data(uint8_t *data, int length, long timestamp) override;

    virtual int stop() override;

    virtual ~SRSRtmpClient();
};


#endif //KU_LIVE_RTMP_CLIENT_H_INCLUDE
