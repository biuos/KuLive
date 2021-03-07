package dai.android.media.live.input

import android.media.MediaCodec
import android.media.MediaCodec.CONFIGURE_FLAG_ENCODE
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Bundle
import android.view.Surface
import dai.android.media.live.AVType
import dai.android.media.live.Package


open class VideoEncoder : AbstractEncoder() {

    var colorFormat: Int = 0
        private set

    var surface: Surface? = null
        private set

    private var lastEncodeTime = 0L


    override fun runnable() {
        if (mediaCodec == null) {
            throw RuntimeException("Bad MediaCodec, call 'config' first")
        }

        presentationTimeUs = System.currentTimeMillis() * 1000
        mediaCodec?.start()

        while (isLoop && !Thread.interrupted()) {
            try {
                if (null != config) {
                    val cfg = config as VideoConfig
                    if (lastEncodeTime != 0L && (System.currentTimeMillis() - lastEncodeTime >= cfg.iFrameInterval * 1000)) {
                        val params = Bundle()
                        params.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0)
                        mediaCodec?.setParameters(params)
                    }
                }

                lastEncodeTime = System.currentTimeMillis();

                if (hasInputPackage()) {
                    val pkt: Package? = packageQueue.take()
                    if (pkt?.buffer == null) continue
                    encode(pkt.buffer)
                } else {
                    encode(null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        mediaCodec?.stop()
        mediaCodec?.release()
        mediaCodec = null
    }

    override fun release() {
    }

    override fun config(config_: AbstractConfig) {
        if (config_.type != AVType.Video)
            return

        config = config_
        val info = config_ as VideoConfig

        mediaCodec = MediaCodec.createEncoderByType(info.mime)
        val format = MediaFormat.createVideoFormat(info.mime, info.width, info.height)
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 0)
        format.setInteger(MediaFormat.KEY_BIT_RATE, info.bitrate)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, info.iFrameInterval)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, info.fps)

        val mediaCodecInfo = getMediaCodecInfoByType(info.mime)
        if (null != mediaCodecInfo) {
            colorFormat = getColorFormat(mediaCodecInfo, info.mime)
        }

        if (colorFormat == 0) {
            colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;
        }
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat)

        mediaCodec?.configure(format, null, null, CONFIGURE_FLAG_ENCODE)
        surface = mediaCodec?.createInputSurface()
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    class VideoConfig : AbstractConfig() {
        override val type: AVType
            get() = AVType.Video

        var width: Int = 0
        var height: Int = 0

        // 视频的帧率
        var fps: Int = 0

        // 视频编码的比特率
        var bitrate: Int = 0

        // KEY_I_FRAME_INTERVAL
        var iFrameInterval = 2

        val mime = MediaFormat.MIMETYPE_VIDEO_AVC
    }
}
