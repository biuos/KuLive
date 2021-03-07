package dai.android.media.live.input

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecInfo.CodecCapabilities
import android.media.MediaCodecList
import android.util.Log
import dai.android.media.live.Package
import java.util.*
import java.util.concurrent.LinkedBlockingQueue


abstract class AbstractEncoder {

    var callback: IEncodeData? = null

    private val bufferInfo = MediaCodec.BufferInfo()
    protected val packageQueue = LinkedBlockingQueue<Package>()

    protected var startTime: Long = 0L
    protected var presentationTimeUs: Long = 0L

    private var lastEncodeTime: Long = 0L

    protected var mediaCodec: MediaCodec? = null
    protected var config: AbstractConfig? = null

    @Volatile
    protected var isLoop = false

    protected abstract fun runnable()

    protected abstract fun release()

    private val workThread = Thread {
        isLoop = true
        runnable()
    }

    abstract fun config(config: AbstractConfig)

    fun addPackage(pkg: Package) {
        packageQueue.add(pkg)
    }

    fun start() {
        workThread.start()
        startTime = System.currentTimeMillis();
    }

    fun stop() {
        isLoop = false
    }

    fun finalize() {
        release()
    }

    protected open fun hasInputPackage(): Boolean {
        return true
    }

    protected fun encode(buffer: ByteArray?) {
        if (null != buffer && hasInputPackage()) {
            val inputBufferId: Int = mediaCodec?.dequeueInputBuffer(-1) ?: -1
            if (inputBufferId >= 0) {
                val bb = mediaCodec?.getInputBuffer(inputBufferId)
                if (null != bb) {
                    bb.clear()
                    bb.put(buffer, 0, buffer.size)
                    val pts = makePTS(presentationTimeUs)
                    mediaCodec?.queueInputBuffer(inputBufferId, 0, buffer.size, pts, 0)
                }
            }
        }

        val outputBufferId = mediaCodec?.dequeueOutputBuffer(bufferInfo, 0) ?: -1
        if (outputBufferId >= 0) {
            val bb = mediaCodec?.getOutputBuffer(outputBufferId)

            if (null != bb && null != callback) {
                val outData = ByteArray(bufferInfo.size)
                bb[outData]
                callback?.outputData(startTime, outData, bufferInfo)
            }
            mediaCodec?.releaseOutputBuffer(outputBufferId, false)
        }
    }


    companion object {
        private const val TAG = "AbstractEncoder"

        fun getMediaCodecInfoByType(mimeType: String): MediaCodecInfo? {
            for (i in 0 until MediaCodecList.getCodecCount()) {
                val codecInfo = MediaCodecList.getCodecInfoAt(i)
                if (!codecInfo.isEncoder) {
                    continue
                }
                val types = codecInfo.supportedTypes
                for (j in types.indices) {
                    if (types[j].equals(mimeType, ignoreCase = true)) {
                        return codecInfo
                    }
                }
            }
            return null
        }

        fun getColorFormat(mediaCodecInfo: MediaCodecInfo, mime: String): Int {
            var matchedFormat = 0
            val codecCapabilities = mediaCodecInfo.getCapabilitiesForType(mime)
            for (i in codecCapabilities.colorFormats.indices) {
                val format = codecCapabilities.colorFormats[i]
                Log.i(TAG, "Color Format: $format")

                if (format == CodecCapabilities.COLOR_FormatSurface) {
                    Log.i(TAG, "Color Format COLOR_FormatSurface selected ")
                    matchedFormat = format
                    break
                }

                if (format == CodecCapabilities.COLOR_FormatYUV420Flexible) {
                    Log.i(TAG, "Color Format COLOR_FormatYUV420Flexible selected ")
                    matchedFormat = format
                    break
                }
            }
            if (matchedFormat <= 0 && codecCapabilities.colorFormats.isNotEmpty()) {
                matchedFormat = codecCapabilities.colorFormats[0]
            }

            return matchedFormat
        }

        fun makePTS(presentationTimeUs: Long): Long {
            return Date().time * 1000 - presentationTimeUs
        }
    }

}