package dai.android.media.live.rtmp2

import dai.android.media.live.MediaConfig
import java.util.*

class FlvMeta {
    private val metaData_ = ArrayList<ByteArray>()
    private var dataSize_ = 0
    private var index_ = 0
    private var metaFrame_: ByteArray? = null


    private fun addProperty(key: ByteArray, dataType: Byte, data: ByteArray) {
        val propertySize = key.size + 1 + data.size
        val property = ByteArray(propertySize)
        System.arraycopy(key, 0, property, 0, key.size)
        property[key.size] = dataType
        System.arraycopy(data, 0, property, key.size + 1, data.size)

        metaData_.add(property)
        dataSize_ += propertySize
    }

    fun setProperty(key: String, value: Int) {
        addProperty(toFLVString(key), 0, toFLVNumber(value.toDouble()))
    }

    fun setProperty(key: String, value: String) {
        addProperty(toFLVString(key), 2, toFLVString(value))
    }


    constructor() {
    }

    constructor(config: MediaConfig) {
        // config for audio AAC
        val audioBitRate = config.audio.bitRate
        val audioSampleRate = config.audio.sampleRate

        // config for video
        val videoFPS = config.video.frameRate
        val videoWidth = config.video.encodeWidth
        val videoHeight = config.video.encodeHeight

        // detail information go to
        // https://blog.csdn.net/yu_yuan_1314/article/details/9358849

        setProperty("audiocodecid", 10)

        when (audioBitRate) {
            32 * 1024 -> setProperty("audiodatarate", 32)
            48 * 1024 -> setProperty("audiodatarate", 48)
            64 * 1024 -> setProperty("audiodatarate", 64)
        }


        if (audioSampleRate == 44100) {
            setProperty("audiosamplerate", audioSampleRate)
        }

        // video property
        setProperty("videocodecid", 7);
        setProperty("framerate", videoFPS);
        setProperty("encodewidth", videoWidth);
        setProperty("encodeheight", videoHeight);
    }

    fun getMeta(): ByteArray {
        metaFrame_ = ByteArray(dataSize_ + EMPTY_SIZE)
        index_ = 0

        // SCRIPTDATA.name
        addByte(2)
        addByteArray(toFLVString(NAME))

        //SCRIPTDATA.value ECMA array
        addByte(8)
        addByteArray(toUI(metaData_.size.toLong(), 4))
        for (pro in metaData_) {
            addByteArray(pro)
        }

        addByteArray(OBJ_END_MARKER)
        return metaFrame_!!
    }

    private fun addByte(value: Int) {
        if (null != metaFrame_) {
            metaFrame_!![index_] = value.toByte()
            index_++
        }
    }

    private fun addByteArray(value: ByteArray) {
        if (null != metaFrame_) {
            System.arraycopy(value, 0, metaFrame_!!, index_, value.size);
            index_ += value.size
        }
    }


    companion object {
        private const val NAME = "onMetaData"

        private const val SCRIPT_DATA = 18
        private const val EMPTY_SIZE = 21

        private val TS_SID = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00) // ???
        private val OBJ_END_MARKER = byteArrayOf(0x00, 0x00, 0x09)                 // ???


        private fun toUI(value: Long, bytes: Int): ByteArray {
            val UI = ByteArray(bytes)
            for (i in 0 until bytes) {
                UI[bytes - 1 - i] = (value shr 8 * i and 0xff).toByte()
            }
            return UI
        }

        private fun toFLVNumber(value: Double): ByteArray {
            val tmp = java.lang.Double.doubleToLongBits(value)
            return toUI(tmp, 8)
        }

        private fun toFLVString(text: String): ByteArray {
            val buffer = ByteArray(text.length + 2)
            System.arraycopy(toUI(text.length.toLong(), 2), 0, buffer, 0, 2)
            System.arraycopy(text.toByteArray(), 0, buffer, 2, text.length)
            return buffer
        }
    }
}