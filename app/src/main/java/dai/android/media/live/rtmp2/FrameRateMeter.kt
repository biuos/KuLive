package dai.android.media.live.rtmp2

class FrameRateMeter {
    private var times = 0
    private var lastFPS = 0f
    private var lastUpdateTime: Long = 0

    fun count() {
        val now = System.currentTimeMillis()
        if (lastUpdateTime == 0L) {
            lastUpdateTime = now
        }
        if (now - lastUpdateTime > TIMETRAVEL_MS) {
            lastFPS = times.toFloat() / (now - lastUpdateTime) * 1000.0f
            lastUpdateTime = now
            times = 0
        }
        ++times
    }

    val fps: Float
        get() = if (System.currentTimeMillis() - lastUpdateTime > GET_TIMETRAVEL_MS) {
            0F
        } else {
            lastFPS
        }

    fun reSet() {
        times = 0
        lastFPS = 0f
        lastUpdateTime = 0
    }

    companion object {
        private const val TIMETRAVEL: Long = 1
        private const val TIMETRAVEL_MS = TIMETRAVEL * 1000
        private const val GET_TIMETRAVEL_MS = 2 * TIMETRAVEL_MS
    }
}