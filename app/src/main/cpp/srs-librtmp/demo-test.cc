
#include <iostream>
#include "srs_librtmp.h"


const char *rtmp_url = "rtmp://live-push.bilivideo.com/live-bvc/?streamname=live_522907740_44689574&key=4005e7f6505f20cf24ea21cefe7b5218&schedule=rtmp";

int main(int argc, char *argv[])
{
    srs_rtmp_t rtmp = srs_rtmp_create(rtmp_url);

    if (srs_rtmp_handshake(rtmp) != 0) {
        srs_human_trace("simple handshake failed.");
        goto rtmp_destroy;
    }

rtmp_destroy:
    srs_rtmp_destroy(rtmp);

    return 0;
}
