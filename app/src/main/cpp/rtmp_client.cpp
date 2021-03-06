
#include "rtmp_client.h"

RtmpClient_SRS::RtmpClient_SRS(const char *url_)
{
    rtmp = srs_rtmp_create(url_);
}

RtmpClient_SRS::~RtmpClient_SRS()
{
    if (!rtmp) {
        srs_rtmp_destroy(rtmp);
        rtmp = nullptr;
    }
}

void RtmpClient_SRS::connect(bool publish)
{
    if (srs_rtmp_handshake(rtmp) != 0) {
        ALOGE("srs_rtmp_handshake failed");
        return;
    }

    if (srs_rtmp_connect_app(rtmp) != 0) {
        ALOGE("srs_rtmp_connect_app failed");
        return;
    }

    if (publish) {
        if (srs_rtmp_publish_stream(rtmp) != 0) {
            ALOGE("srs_rtmp_publish_stream failed");
            return;
        }
    }
}

int RtmpClient_SRS::audioRawFrameWrite(
    char format, char rate, char size, char type,
    char *frame, int frame_size,
    u_int32_t timestamp)
{
    if (!rtmp) {
        ALOGE("nullptr of rtmp client when 'audio_write_raw_frame'");
        return -1;
    }

    return srs_audio_write_raw_frame(
               rtmp,
               format, rate, size, type,
               frame,
               frame_size, timestamp);
}

int
RtmpClient_SRS::videoH264RawFramesWrite(char *frames, int frames_size, u_int32_t dts, u_int32_t pts)
{
    if (!rtmp) {
        ALOGE("nullptr of rtmp client when 'h264_write_raw_frames'");
        return -1;
    }

    return srs_h264_write_raw_frames(rtmp, frames, frames_size, dts, pts);
}

int RtmpClient_SRS::writePacket(int type, uint32_t timestamp, char *data, int size)
{
    if (!rtmp) {
        ALOGE("nullptr of rtmp client when 'writePacket'");
        return -1;
    }

    switch (type) {
        case SRS_RTMP_TYPE_AUDIO:
        case SRS_RTMP_TYPE_VIDEO:
        case SRS_RTMP_TYPE_SCRIPT: {
                if (srs_rtmp_write_packet(rtmp, type, timestamp, data, size) != 0) {
                    ALOGE("srs_rtmp_write_packet failed");
                    return -1;
                }
                return 0;
            }
        default: {
                ALOGE("unknown type %d", type);
                break;
            }
    }

    return -1;
}


//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// implement class RtmpClient
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

#include <cstring>
#include <malloc.h>

RtmpClient::RtmpClient(const char *url_)
{
    rtmp = RTMP_Alloc();
    if(nullptr == rtmp) {
        ALOGE("RTMP_Alloc failed.");
        return;
    }

    RTMP_Init(rtmp);
    rtmp->Link.timeout = 15;
    if(TRUE != RTMP_SetupURL(rtmp, (char *)url_) ) {
        RTMP_Free(rtmp);
        rtmp = nullptr;

        ALOGE("RTMP_SetupURL failed");
        return;
    }
}

RtmpClient::~RtmpClient()
{
    if(nullptr != rtmp) {
        RTMP_Free(rtmp);
        rtmp = nullptr;
    }

    if(vPackage) {
        if(vPackage->pps) {
            free(vPackage->pps);
            vPackage->pps = nullptr;
        }
        if(vPackage->sps) {
            free(vPackage->sps);
            vPackage->sps = nullptr;
        }

        delete vPackage;
        vPackage = nullptr;
    }

    ALOGI("RtmpClient.RTMP_Free");
}

void RtmpClient::connect(bool publish)
{
    if(!rtmp) {
        ALOGE("No rtmp client, disable");
        return;
    }

    if(publish)
        RTMP_EnableWrite(rtmp);

    if( TRUE != RTMP_Connect(rtmp, nullptr) ) {
        ALOGE("RTMP_Connect failed");
        return;
    }

    if( TRUE != RTMP_ConnectStream(rtmp, 0) ) {
        ALOGE("RTMP_ConnectStream failed");
        return;
    }

    ALOGI("connect success");
}

int RtmpClient::audioRawFrameWrite(
    char format, char rate, char size, char type,
    char *frame, int frame_size,
    u_int32_t timestamp)
{
    return -1;
}

int RtmpClient::videoH264RawFramesWrite(char *frames, int frames_size, u_int32_t dts, u_int32_t pts)
{
    return FALSE;
}

int RtmpClient::writePacket(int type, uint32_t timestamp, char *data, int size)
{
    switch (type) {
        case RTMP_PACKET_TYPE_VIDEO: {
                writeVideoPackage(data, size, timestamp);
                break;
            }

        case RTMP_PACKET_TYPE_AUDIO: {
                writeAudioPackage(data, size, timestamp);
                break;
            }

        default: {
                ALOGE("unknown package type=%d timestamp=%ld", type, timestamp);
                break;
            }
    }
    return FALSE;
}

RTMPPacket *RtmpClient::makeVideoPackage(int8_t *buffer, int length, const long timestamp)
{
    if(!rtmp) {
        ALOGE("nullptr object of RTMP");
        return nullptr;
    }

    buffer += 4;
    length -= 4;

    int   body_size = length + 9;
    auto *pkt       =  (RTMPPacket *)malloc(sizeof(RTMPPacket));
    RTMPPacket_Alloc(pkt, length + 9);

    // I frame
    if(buffer[0] == 0x65) {
        pkt->m_body[0] = 0x17;
        ALOGI("I frame at timestamp=%ld", timestamp);
    }
    // NOT I frame
    else {
        pkt->m_body[0] = 0x27;
    }

    // write annexb
    pkt->m_body[1] = 0x01;
    pkt->m_body[2] = 0x00;
    pkt->m_body[3] = 0x00;
    pkt->m_body[4] = 0x00;

    // write data length
    pkt->m_body[5] = (length >> 24) & 0xFF;
    pkt->m_body[6] = (length >> 16) & 0xFF;
    pkt->m_body[7] = (length >>  8) & 0xFF;
    pkt->m_body[8] = (length      ) & 0xFF;

    // copy data
    memcpy(&pkt->m_body[9], buffer, length);

    pkt->m_packetType      = RTMP_PACKET_TYPE_VIDEO;
    pkt->m_nBodySize       = body_size;
    pkt->m_nChannel        = 0x04;
    pkt->m_nTimeStamp      = timestamp;
    pkt->m_hasAbsTimestamp = 0;
    pkt->m_headerType      = RTMP_PACKET_SIZE_LARGE;
    pkt->m_nInfoField2     = rtmp->m_stream_id;

    return pkt;
}


RTMPPacket *RtmpClient::makeVideoPackage(VideoPackage *package)
{
    if(!package || !rtmp) {
        ALOGE("nullpatr install RTMP or VideoPackage");
        return nullptr;
    }

    int   body_size = 13 + package->sps_length + 3 + package->pps_length;
    auto *pkt       = (RTMPPacket *)malloc(sizeof(RTMPPacket));
    RTMPPacket_Alloc(pkt, body_size);

    int i = 0;
    // AVC sequence header  IDR
    pkt->m_body[i++] = 0x17;

    // AVC sequence header setting to 0x00
    pkt->m_body[i++] = 0x00;

    // composition time
    pkt->m_body[i++] = 0x00;
    pkt->m_body[i++] = 0x00;
    pkt->m_body[i++] = 0x00;

    // AVC sequence header
    pkt->m_body[i++] = 0x01;            // configuration version
    pkt->m_body[i++] = package->sps[1]; // profile baseline main high
    pkt->m_body[i++] = package->sps[2]; // profile compatibility
    pkt->m_body[i++] = package->sps[3]; // profile level

    pkt->m_body[i++] = 0xFF;
    pkt->m_body[i++] = 0xE1;

    pkt->m_body[i++] = (package->sps_length >> 8) & 0xff;
    pkt->m_body[i++] =  package->sps_length       & 0xff;

    memcpy(&pkt->m_body[i], package->sps, package->sps_length);
    i += package->sps_length;

    // pps
    pkt->m_body[i++] = 0x01;
    pkt->m_body[i++] = (package->pps_length >> 8) & 0xff;
    pkt->m_body[i++] =  package->pps_length       & 0xff;
    memcpy(&pkt->m_body[i], package->pps, package->pps_length);


    pkt->m_packetType      = RTMP_PACKET_TYPE_VIDEO;
    pkt->m_nBodySize       = body_size;
    pkt->m_nChannel        = 0x04;
    pkt->m_nTimeStamp      = 0;
    pkt->m_hasAbsTimestamp = 0;
    pkt->m_headerType      = RTMP_PACKET_SIZE_LARGE;
    pkt->m_nInfoField2     = rtmp->m_stream_id;

    return pkt;
}

void RtmpClient::preMakeVideo(int8_t *buffer, int length, VideoPackage *package)
{
    if(!buffer || length <= 0 || !package) {
        ALOGE("nullptr object when pre make video");
        return;
    }

    for(int i = 0; i < length; ++i) {
        // 0x00    0x00    0x00    0x01
        if(i + 4 < length) {
            if( buffer[0] == 0x00 && buffer[1] == 0x00 && buffer[2] == 0x00 && buffer[3] == 0x01 ) {
                // 0x00 0x00 0x00 0x01   7  --sps
                // 0x00 0x00 0x00 0x01   8  --pps
                if(buffer[i + 4] == 0x68) {
                    package->sps_length = i - 4;
                    package->sps        = static_cast<int8_t *>(malloc(package->sps_length));
                    memcpy(package->sps, buffer + 4, package->sps_length);

                    package->pps_length = length - (4 + package->sps_length) - 4;
                    package->pps        = static_cast<int8_t *>(malloc(package->pps_length));
                    memcpy(package->pps, buffer + 4 + package->sps_length + 4, package->pps_length);

                    break;
                }
            }
        }
    }
}

void RtmpClient::writeAudioPackage(char *data, int size, uint32_t timestamp)
{

}

void RtmpClient::writeVideoPackage(char *data, int size, uint32_t timestamp)
{
    if(!data || size <= 0 || !rtmp ) {
        ALOGE("null data or empty");
        return;
    }

    // sps  pps
    if(data[4] == 0x67) {
        if(vPackage) {
            if(vPackage->pps) {
                free(vPackage->pps);
                vPackage->pps = nullptr;
            }
            if(vPackage->sps) {
                free(vPackage->sps);
                vPackage->sps = nullptr;
            }

            delete vPackage;
            vPackage = nullptr;
        }
        vPackage = new VideoPackage;

        preMakeVideo((int8_t *)data, size, vPackage);
    }
    // normal data
    else {
        // I frame
        if(data[4] == 0x65) {
            auto *pkt = makeVideoPackage(vPackage);
            if( TRUE !=  RTMP_SendPacket(rtmp, pkt, 1) ) {
                ALOGE("RTMP_SendPacket I frame key failed");
            }
            RTMPPacket_Free(pkt);
            free(pkt);
        }

        auto *pkt = makeVideoPackage(reinterpret_cast<int8_t *>(data), size, timestamp );
        if( TRUE !=  RTMP_SendPacket(rtmp, pkt, 1) ) {
            ALOGE("RTMP_SendPacket normal frame failed");
        }
        RTMPPacket_Free(pkt);
        free(pkt);
    }
}

