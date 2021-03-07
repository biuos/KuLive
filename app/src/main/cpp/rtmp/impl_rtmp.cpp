#include "IRtmpClient.h"

#include "Logger.h"

#include <cstring>
#include <cstdlib>

#define RTMP_HEAD_SIZE ( sizeof(RTMPPacket) + RTMP_MAX_HEADER_SIZE )
#define NAL_SLICE      1
#define NAL_SLICE_DPA  2
#define NAL_SLICE_DPB  3
#define NAL_SLICE_DPC  4
#define NAL_SLICE_IDR  5
#define NAL_SEI        6
#define NAL_SPS        7
#define NAL_PPS        8
#define NAL_AUD        9
#define NAL_FILLER    12

#define STREAM_CHANNEL_METADATA  0x03
#define STREAM_CHANNEL_VIDEO     0x04
#define STREAM_CHANNEL_AUDIO     0x05


int RtmpClient::init(const char *c_url)
{
    rtmp = RTMP_Alloc();
    RTMP_Init(rtmp);

    rtmp->Link.timeout = 30;
    RTMP_SetupURL(rtmp, (char *) c_url);
    RTMP_EnableWrite(rtmp);

    if (!RTMP_Connect(rtmp, nullptr)) {
        ALOGE("RTMP_Connect error");
        return -1;
    }
    ALOGI("RTMP_Connect success.");

    if (!RTMP_ConnectStream(rtmp, 0)) {
        ALOGI("RTMP_ConnectStream error");
        return -1;
    }

    ALOGI("RTMP_ConnectStream success.");

    return 0;
}

void RtmpClient::free_sps_pps()
{
    if (sps) free(sps);
    sps       = nullptr;
    spsLength = 0;

    if (pps) free(pps);
    pps = nullptr;
    ppsLength = 0;
}

bool RtmpClient::do_sps_pps(const uint8_t *buffer, int length)
{
    if (!buffer || length <= 4)
        return false;

    // sps pps
    if (buffer[4] == 0x67) {
        for (int i = 0; i < length; ++i) {
            if (i + 4 < length) {
                if( buffer[0] == 0x00 && buffer[1] == 0x00 && buffer[2] == 0x00 && buffer[3] == 0x01 ) {
                    // 0x00 0x00 0x00 0x01   7  --sps
                    // 0x00 0x00 0x00 0x01   8  --pps
                    if(buffer[i + 4] == 0x68) {
                        free_sps_pps();

                        spsLength = i - 4;
                        sps       = (uint8_t *)malloc(spsLength);
                        memcpy(sps, buffer + 4, spsLength);

                        ppsLength = length - (4 + spsLength) - 4;
                        pps       = (uint8_t *) malloc(ppsLength);
                        memcpy(pps, buffer + 4 + spsLength + 4, ppsLength);

                        return true;
                    }
                }
            }
        }
    }
    return false;
}

int RtmpClient::write_sps_pps(
    uint8_t *sps, int spsLength,
    uint8_t *pps, int ppsLength,
    long timestamp)
{
    auto *packet   = (RTMPPacket *) malloc(RTMP_HEAD_SIZE + 1024);
    memset(packet, 0, RTMP_HEAD_SIZE);
    packet->m_body = (char *) packet + RTMP_HEAD_SIZE;
    auto *body     = (uint8_t *) packet->m_body;

    int     i = 0;
    body[i++] = 0x17; //1:keyframe 7:AVC
    body[i++] = 0x00; // AVC sequence header

    body[i++] = 0x00;
    body[i++] = 0x00;
    body[i++] = 0x00; //fill in 0

    /*AVCDecoderConfigurationRecord*/
    body[i++] = 0x01;
    body[i++] = sps[1]; //AVCProfileIndecation
    body[i++] = sps[2]; //profile_compatibilty
    body[i++] = sps[3]; //AVCLevelIndication
    body[i++] = 0xff;//lengthSizeMinusOne

    // sps
    body[i++] = 0xe1;
    body[i++] = (spsLength >> 8) & 0xff;
    body[i++] =  spsLength       & 0xff;
    // sps data
    memcpy(&body[i], sps, spsLength);

    i += spsLength;

    // pps
    body[i++] = 0x01;
    // pps data length
    body[i++] = (ppsLength >> 8) & 0xff;
    body[i++] =  ppsLength       & 0xff;
    memcpy(&body[i], pps, ppsLength);
    i += ppsLength;

    packet->m_packetType      = RTMP_PACKET_TYPE_VIDEO;
    packet->m_nBodySize       = i;
    packet->m_nChannel        = 0x04;
    packet->m_nTimeStamp      = 0;
    packet->m_hasAbsTimestamp = 0;
    packet->m_headerType      = RTMP_PACKET_SIZE_MEDIUM;
    packet->m_nInfoField2     = rtmp->m_stream_id;

    if (RTMP_IsConnected(rtmp)) {
        RTMP_SendPacket(rtmp, packet, TRUE);
    }
    RTMPPacket_Free(packet);
    free(packet);

    return 0;
}

int RtmpClient::write_video_data(uint8_t *data, int length, long timestamp)
{

    if(do_sps_pps(data, length)) {
        // write_sps_pps(sps, spsLength, pps, ppsLength, timestamp);
        return 0;
    }

    if(data[4] == 0x65) {
        write_sps_pps(sps, spsLength, pps, ppsLength, timestamp);
    }

    // 去掉帧界定符
    if (data[2] == 0x00) { // 00 00 00 01
        data   += 4;
        length -= 4;
    } else if (data[2] == 0x01) {
        data   += 3;
        length -= 3;
    }

    int type = data[0] & 0x1f;

    auto *packet = (RTMPPacket *) malloc(RTMP_HEAD_SIZE + length + 9);
    memset(packet, 0, RTMP_HEAD_SIZE);
    packet->m_body      = (char *) packet + RTMP_HEAD_SIZE;
    packet->m_nBodySize = length + 9;


    // send video packet
    auto *body = (uint8_t *) packet->m_body;
    memset(body, 0, length + 9);

    // key frame
    body[0] = 0x27;
    if (type == NAL_SLICE_IDR) {
        body[0] = 0x17; // I frame
    }

    body[1] = 0x01; // nal unit
    body[2] = 0x00;
    body[3] = 0x00;
    body[4] = 0x00;

    body[5] = (length >> 24) & 0xff;
    body[6] = (length >> 16) & 0xff;
    body[7] = (length >>  8) & 0xff;
    body[8] = (length      ) & 0xff;

    memcpy(&body[9], data, length);

    packet->m_hasAbsTimestamp = 0;
    packet->m_packetType      = RTMP_PACKET_TYPE_VIDEO;
    packet->m_nInfoField2     = rtmp->m_stream_id;
    packet->m_nChannel        = 0x04;
    packet->m_headerType      = RTMP_PACKET_SIZE_LARGE;
    packet->m_nTimeStamp      = timestamp;

    if (RTMP_IsConnected(rtmp)) {
        RTMP_SendPacket(rtmp, packet, TRUE);
    }
    RTMPPacket_Free(packet);
    free(packet);

    return 0;
}

int RtmpClient::write_aac_spec(uint8_t *data, int length)
{
    RTMPPacket *packet;
    uint8_t    *body;
    int len = length;//spec len 是2
    packet  = (RTMPPacket *) malloc(RTMP_HEAD_SIZE + len + 2);
    memset(packet, 0, RTMP_HEAD_SIZE);
    packet->m_body = (char *) packet + RTMP_HEAD_SIZE;
    body           = (uint8_t *) packet->m_body;

    // AF 00  +  AAC RAW data
    body[0] = 0xAF;
    body[1] = 0x00;
    memcpy(&body[2], data, len); // data 是AAC sequeuece header数据

    packet->m_packetType      = RTMP_PACKET_TYPE_AUDIO;
    packet->m_nBodySize       = len + 2;
    packet->m_nChannel        = STREAM_CHANNEL_AUDIO;
    packet->m_nTimeStamp      = 0;
    packet->m_hasAbsTimestamp = 0;
    packet->m_headerType      = RTMP_PACKET_SIZE_LARGE;
    packet->m_nInfoField2     = rtmp->m_stream_id;

    if (RTMP_IsConnected(rtmp)) {
        RTMP_SendPacket(rtmp, packet, TRUE);
    }
    free(packet);

    return 0;
}

int RtmpClient::write_aac_data(uint8_t *data, int length, long timestamp)
{
    if (length > 0) {
        RTMPPacket *packet;
        uint8_t    *body;
        packet = (RTMPPacket *) malloc(RTMP_HEAD_SIZE + length + 2);
        memset(packet, 0, RTMP_HEAD_SIZE);
        packet->m_body = (char *) packet + RTMP_HEAD_SIZE;
        body           = (uint8_t *) packet->m_body;

        // AF 00 +AAC Raw data
        body[0] = 0xAF;
        body[1] = 0x01;
        memcpy(&body[2], data, length);

        packet->m_packetType      = RTMP_PACKET_TYPE_AUDIO;
        packet->m_nBodySize       = length + 2;
        packet->m_nChannel        = STREAM_CHANNEL_AUDIO;
        packet->m_nTimeStamp      = timestamp;
        packet->m_hasAbsTimestamp = 0;
        packet->m_headerType      = RTMP_PACKET_SIZE_LARGE;
        packet->m_nInfoField2     = rtmp->m_stream_id;
        if (RTMP_IsConnected(rtmp)) {
            RTMP_SendPacket(rtmp, packet, TRUE);
        }
        //LOGD("send packet body[0]=%x,body[1]=%x", body[0], body[1]);
        RTMPPacket_Free(packet);
        free(packet);
    }
    return 0;
}

int RtmpClient::stop()
{
    if (rtmp) {
        RTMP_Close(rtmp);
        RTMP_Free(rtmp);
    }

    rtmp = nullptr;

    return 0;
}

RtmpClient::~RtmpClient()
{
    stop();
    free_sps_pps();
}
