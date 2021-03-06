package dai.android.media.live.rtmp2

object RtmpClient {

    external fun open(url: String?, isPublishMode: Boolean): Long

    external fun read(handler: Long, data: ByteArray?, offset: Int, size: Int): Int

    external fun write(handler: Long, data: ByteArray?, size: Int, type: Int, ts: Int): Int

    external fun close(handler: Long): Int

    external fun getIpAddr(handler: Long): String?

    init {
        System.loadLibrary("publisher")
    }
}
