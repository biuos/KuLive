package dai.android.media.live

abstract class Package {
    var buffer: ByteArray? = null

    abstract val type: AVType
}
