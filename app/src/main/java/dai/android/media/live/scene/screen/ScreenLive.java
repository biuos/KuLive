package dai.android.media.live.scene.screen;

import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.os.Bundle;
import android.view.Surface;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;

import dai.android.media.live.Package;
import dai.android.media.live.input.AbstractConfig;
import dai.android.media.live.input.IEncodeData;
import dai.android.media.live.input.VideoEncoder;
import dai.android.media.live.output.AbstractLive;
import dai.android.media.live.output.OutAPackage;
import dai.android.media.live.output.OutVPackage;

import static dai.android.media.live.AVType.Audio;
import static dai.android.media.live.AVType.Video;

public class ScreenLive extends AbstractLive {

    //private final VideoCodec videoCodec;

    private final ScreenEncode videoEncoder;

    private final IEncodeData videoCallback = (startTime, bb, info) -> {
        OutVPackage pkt = new OutVPackage();
        pkt.setTimestamp(info.presentationTimeUs);
        pkt.setBuffer(bb);
        addPackage(pkt);
    };

    public ScreenLive(String url, MediaProjection mp) {
        super(url);

        videoEncoder = new ScreenEncode(mp);
        videoEncoder.setCallback(videoCallback);
    }

    @Override
    protected void work() {
        rtmp.connect(true);

        VideoEncoder.VideoConfig config = new VideoEncoder.VideoConfig();
        config.setBitrate(150_000);
        config.setFps(15);
        config.setWidth(1080);
        config.setHeight(1920);
        config.setIFrameInterval(2);
        videoEncoder.config(config);

        // start the codec
        videoEncoder.start();

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

            if (pkt.getType() == Audio) {
                writeAudioPackage((OutAPackage) pkt);
            } else if (pkt.getType() == Video) {
                writeVideoPackage((OutVPackage) pkt);
            }
        }
    }

    @Override
    protected void subRelease() {
    }

    @Override
    protected void subStop() {
        videoEncoder.stop();
    }

    @Override
    protected void subStart() {
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // class
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    static class ScreenEncode extends VideoEncoder {
        private final MediaProjection mediaProjection;
        private VirtualDisplay virtualDisplay;

        ScreenEncode(MediaProjection mp) {
            mediaProjection = mp;
        }

        @Override
        protected boolean hasInputPackage() {
            return false;
        }

        @Override
        public void config(@NotNull AbstractConfig config) {
            super.config(config);

            virtualDisplay = mediaProjection.createVirtualDisplay(
                    "screen-codec",
                    1080, 1920, 1,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                    getSurface(), null, null);

        }

        @Override
        protected void runnable() {
            super.runnable();

            virtualDisplay.release();
            virtualDisplay = null;

            mediaProjection.stop();
        }
    }


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
                    OutVPackage pkt = new OutVPackage();
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
            MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 680, 720);
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
                        580, 720, 1,
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
