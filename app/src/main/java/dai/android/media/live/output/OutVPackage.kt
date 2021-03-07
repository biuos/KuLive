package dai.android.media.live.output

import dai.android.media.live.AVType
import dai.android.media.live.Package

class OutVPackage : Package() {
    var dts = 0L
    var pts = 0L
    var timestamp = 0L

    override val type: AVType
        get() = AVType.Video
}
