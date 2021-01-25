package dai.android.media.live

import android.media.AudioFormat

class MediaConfig private constructor(val url: String, val video: Video, val audio: Audio) {

    class Builder(private val url: String) {

        private lateinit var video: Video
        private lateinit var audio: Audio

        fun setVideo(video_: Video): Builder {
            video = video_
            return this
        }

        fun setAudio(audio_: Audio): Builder {
            audio = audio_
            return this
        }

        fun build(): MediaConfig {
            return MediaConfig(url, video, audio)
        }
    }


    data class Video(
        val mime: String,
        val previewWidth: Int,
        val previewHeight: Int,
        val encodeWidth: Int,
        val encodeHeight: Int,
        val frameRate: Int = 15,
        val iFrame: Int = 1,
        val colorFormat: Int
    )

    data class Audio(
        val mime: String,
        val sampleRate: Int = 44100,
        val channelConfig: Int = AudioFormat.CHANNEL_IN_MONO,
        val channelCount: Int = 1,
        val bitRate: Int = 128000,
        val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT
    )

    class AudioBuilder {
        private var mime: String = "audio/mp4a-latm"
        private var sampleRate: Int = 44100
        private var channelConfig: Int = AudioFormat.CHANNEL_IN_MONO
        private var channelCount: Int = 1
        private var bitRate: Int = 128000
        private var audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT

        fun setMime(mime_: String): AudioBuilder {
            mime = mime_
            return this
        }

        fun setSampleRate(rate: Int): AudioBuilder {
            sampleRate = rate
            return this
        }

        fun setChannelConfig(config: Int): AudioBuilder {
            channelConfig = config
            return this
        }

        fun setChannelCount(count: Int): AudioBuilder {
            channelCount = count
            return this
        }

        fun setBitRate(rate: Int): AudioBuilder {
            bitRate = rate
            return this
        }

        fun setAudioFormat(format: Int): AudioBuilder {
            audioFormat = format
            return this
        }

        fun build(): Audio {
            return Audio(
                mime = mime, sampleRate = sampleRate,
                channelConfig = channelConfig, channelCount = channelCount,
                bitRate = bitRate,
                audioFormat = audioFormat
            )
        }
    }


    class VideoBuilder {
        private var mime: String = "video/avc"
        private var previewWidth: Int = 720
        private var previewHeight: Int = 1080
        private var encodeWidth: Int = 720
        private var encodeHeight: Int = 1080
        private var frameRate: Int = 15
        private var iFrame: Int = 1
        private var colorFormat: Int = 0

        fun setMime(mime_: String): VideoBuilder {
            mime = mime_
            return this
        }

        fun setPreviewWidth(width: Int): VideoBuilder {
            previewWidth = width
            return this
        }

        fun setPreviewHeight(height: Int): VideoBuilder {
            previewHeight = height
            return this
        }

        fun setEncodeWidth(width: Int): VideoBuilder {
            encodeWidth = width
            return this
        }

        fun setEncodeHeight(height: Int): VideoBuilder {
            encodeHeight = height
            return this
        }

        fun setFrameRate(rate: Int): VideoBuilder {
            frameRate = rate
            return this
        }

        fun setIFrame(i: Int): VideoBuilder {
            iFrame = i
            return this
        }

        fun setColorFormat(format: Int): VideoBuilder {
            colorFormat = format
            return this
        }

        fun build(): Video {
            return Video(
                mime = mime,
                previewWidth = previewWidth, previewHeight = previewHeight,
                encodeWidth = encodeWidth, encodeHeight = encodeHeight,
                frameRate = frameRate,
                iFrame = iFrame,
                colorFormat = colorFormat
            )
        }
    }
}
