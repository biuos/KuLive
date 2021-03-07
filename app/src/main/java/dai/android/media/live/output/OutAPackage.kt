package dai.android.media.live.output

import dai.android.media.live.AVType
import dai.android.media.live.Package

class OutAPackage : Package() {
    override val type: AVType
        get() = AVType.Audio
}

