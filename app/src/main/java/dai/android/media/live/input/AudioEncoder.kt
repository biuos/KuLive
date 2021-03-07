package dai.android.media.live.input

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaFormat.KEY_BIT_RATE
import android.media.MediaFormat.KEY_MAX_INPUT_SIZE
import dai.android.media.live.AVType
import dai.android.media.live.Package

class AudioEncoder : AbstractEncoder() {

    override fun runnable() {
        if (mediaCodec == null) {
            throw RuntimeException("Bad MediaCodec, call 'config' first")
        }

        presentationTimeUs = System.currentTimeMillis() * 1000
        mediaCodec?.start()

        while (isLoop && !Thread.interrupted()) {
            try {
                val pkt: Package? = packageQueue.take()
                if (pkt?.buffer == null)
                    continue

                encode(pkt.buffer!!)

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

    override fun config(config: AbstractConfig) {
        if (config.type != AVType.Video)
            return

        val info = config as AudioConfig

        mediaCodec = MediaCodec.createEncoderByType(info.mime)
        val format = MediaFormat.createAudioFormat(info.mime, info.sampleRate, info.chanelCount)
        format.setInteger(KEY_MAX_INPUT_SIZE, 0);
        format.setInteger(KEY_BIT_RATE, info.sampleRate * info.chanelCount)
        mediaCodec?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    }


    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    class AudioConfig : AbstractConfig() {
        override val type: AVType
            get() = AVType.Audio

        var chanelCount: Int = 0
        var sampleRate: Int = 0

        val mime = MediaFormat.MIMETYPE_AUDIO_AAC
    }
}
