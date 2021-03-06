package dai.android.media.live.output

class VideoPackage : Package() {
    var dts = 0L
    var pts = 0L
    var timestamp = 0L

    override fun getType(): PacketType {
        return PacketType.Video
    }
}
