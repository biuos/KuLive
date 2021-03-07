package dai.android.media.live.input

import dai.android.media.live.AVType
import dai.android.media.live.Package

class InVPackage : Package() {
    override val type: AVType
        get() = AVType.Video
}
