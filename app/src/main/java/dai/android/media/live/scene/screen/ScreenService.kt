package dai.android.media.live.scene.screen

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import dai.android.media.live.MainActivity
import dai.android.media.live.R
import dai.android.media.live.output.RtmpPublisher
import java.util.*

class ScreenService : Service() {

    private var liver: ScreenLive? = null

    private var mediaProjectionManager: MediaProjectionManager? = null
    private var mediaProjection: MediaProjection? = null


    override fun onBind(intent: Intent): IBinder? {
        Log.i(TAG, "onBind")
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand")

        makeNotificationChannel()

        val code = intent.getIntExtra("code", -1)
        val data = intent.getParcelableExtra<Intent>("data")

        val mgr = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val mediaProjection = Objects.requireNonNull(data)?.let { mgr.getMediaProjection(code, it) }

        liver = ScreenLive(RtmpPublisher.RTMP_URL, mediaProjection)
        liver?.start()

        return super.onStartCommand(intent, flags, startId)
    }

    private fun makeNotificationChannel() {
        val builder = Notification.Builder(applicationContext)
        val intent = Intent(this, MainActivity::class.java)
        builder.setContentIntent(PendingIntent.getActivity(this, 0, intent, 0))
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentText("Screen Live now ...")
            .setWhen(System.currentTimeMillis())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(NOTIFICATION_ID)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                NOTIFICATION_ID,
                NOTIFICATION_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = builder.build()
        notification.defaults = Notification.DEFAULT_SOUND
        startForeground(110, notification)
    }


    companion object {
        const val TAG = "ScreenService"
        const val NOTIFICATION_ID = "ID_KU_SCREEN_LIVE"
        const val NOTIFICATION_NAME = "Screen Live"
    }
}
