package dai.android.media.live.rtmp

const val FLV_RTMP_PKT_TYPE_VIDEO = 9
const val FLV_RTMP_PKT_TYPE_AUDIO = 8
const val FLV_RTMP_PKT_TYPE_INFO = 18
const val NALU_TYPE_IDR = 5

data class FlvRaw(
    val dts: Long,
    val byteBuffer: ByteArray,
    val size: Int,
    val flvTagType: Int,
    val videoFrameType: Int
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FlvRaw

        if (dts != other.dts) return false
        if (!byteBuffer.contentEquals(other.byteBuffer)) return false
        if (size != other.size) return false
        if (flvTagType != other.flvTagType) return false
        if (videoFrameType != other.videoFrameType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = dts.hashCode()
        result = 31 * result + byteBuffer.contentHashCode()
        result = 31 * result + size
        result = 31 * result + flvTagType
        result = 31 * result + videoFrameType
        return result
    }
}


fun isVideoKeyFrame(raw: FlvRaw): Boolean {
    return raw.videoFrameType == NALU_TYPE_IDR
}
