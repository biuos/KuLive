#include "IRtmpClient.h"

int SRSRtmpClient::init(const char *c_url)
{
    return 0;
}

int SRSRtmpClient::write_sps_pps(uint8_t *sps, int spsLength,
                                 uint8_t *pps, int ppsLength,
                                 long timestamp)
{
    return 0;
}

int SRSRtmpClient::write_video_data(uint8_t *data, int length, long timestamp)
{
    return 0;
}

int SRSRtmpClient::write_aac_spec(uint8_t *data, int length)
{
    return 0;
}

int SRSRtmpClient::write_aac_data(uint8_t *data, int length, long timestamp)
{
    return 0;
}

int SRSRtmpClient::stop()
{
    return 0;
}

SRSRtmpClient::~SRSRtmpClient()
{
}
