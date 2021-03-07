package dai.android.media.live.input

import dai.android.media.live.AVType
import dai.android.media.live.Package

class InAPackage : Package() {

    override val type: AVType
        get() = AVType.Audio
}
