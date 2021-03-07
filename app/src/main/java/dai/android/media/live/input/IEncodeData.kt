package dai.android.media.live.input

import android.media.MediaCodec
import java.nio.ByteBuffer

interface IEncodeData {
    fun outputData(startTime: Long, bb: ByteArray, info: MediaCodec.BufferInfo)
}
