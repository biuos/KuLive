package dai.android.media.live.scene.screen;

import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.os.Bundle;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

import dai.android.media.live.output.AbstractLive;
import dai.android.media.live.output.AudioPackage;
import dai.android.media.live.output.Package;
import dai.android.media.live.output.VideoPackage;

public class ScreenLive extends AbstractLive {

    private final VideoCodec videoCodec;

    public ScreenLive(String url, MediaProjection mp) {
        super(url);

        videoCodec = new VideoCodec(this, mp);
    }

    @Override
    protected void work() {
        rtmp.connect(true);

        // start the codec
        videoCodec.start();

        while (started) {
            Package pkt = null;
            try {
                pkt = pktQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (null == pkt) {
                continue;
            }

            if (pkt.getType() == Package.PacketType.Audio) {
                writeAudioPackage((AudioPackage) pkt);
            } else if (pkt.getType() == Package.PacketType.Video) {
                writeVideoPackage((VideoPackage) pkt);
            }
        }
    }

    @Override
    protected void subRelease() {
    }

    @Override
    protected void subStop() {
        videoCodec.stop();
    }

    @Override
    protected void subStart() {
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // class
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    static class VideoCodec {

        private final ScreenLive screenLive;
        private final MediaProjection mediaProjection;
        private VirtualDisplay virtualDisplay;

        private MediaCodec mediaCodec;

        private volatile boolean started = false;

        private long startTime = 0L;
        private long tms = 0L;

        private void work() {
            if (null == mediaCodec)
                return;

            mediaCodec.start();
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

            while (started) {
                // make a I frame every 2 second
                if (tms != 0 && (System.currentTimeMillis() - tms >= 2_000)) {
                    Bundle params = new Bundle();
                    params.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0);
                    mediaCodec.setParameters(params);
                }
                tms = System.currentTimeMillis();

                int index = mediaCodec.dequeueOutputBuffer(bufferInfo, 10);
                if (index >= 0) {
                    ByteBuffer buffer = mediaCodec.getOutputBuffer(index);
                    byte[] outData = new byte[bufferInfo.size];
                    buffer.get(outData);

                    /// ByteBuffer sps = mediaCodec.getOutputFormat().getByteBuffer("csd-0");
                    /// ByteBuffer pps = mediaCodec.getOutputFormat().getByteBuffer("csd-1");

                    if (startTime == 0) {
                        startTime = bufferInfo.presentationTimeUs / 1000;
                    }
                    VideoPackage pkt = new VideoPackage();
                    pkt.setBuffer(outData);
                    long tms = (bufferInfo.presentationTimeUs / 1000) - startTime;
                    pkt.setTimestamp(tms);
                    screenLive.addPackage(pkt);
                    mediaCodec.releaseOutputBuffer(index, false);
                }
            }

            startTime = 0L;
            mediaCodec.stop();
            mediaCodec.release();
            mediaCodec = null;


            virtualDisplay.release();
            virtualDisplay = null;

            mediaProjection.stop();
        }

        private final Thread codecThread = new Thread(() -> {
            started = true;
            work();
        });

        VideoCodec(ScreenLive sl, MediaProjection mp) {
            screenLive = sl;
            mediaProjection = mp;
        }

        void start() {
            MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 540, 960);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            format.setInteger(MediaFormat.KEY_BIT_RATE, 400_000);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2);
            try {
                mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
                mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                Surface surface = mediaCodec.createInputSurface();
                virtualDisplay = mediaProjection.createVirtualDisplay(
                        "screen-codec",
                        540, 960, 1,
                        DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                        surface, null, null);

            } catch (IOException e) {
                e.printStackTrace();
            }


            codecThread.start();
        }

        void stop() {
            started = false;
        }

    }


}
