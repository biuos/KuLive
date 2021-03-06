package dai.android.media.live

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import dai.android.media.live.output.RtmpPublisher
import dai.android.media.live.scene.screen.ScreenLive


class MainActivity : AppCompatActivity() {

    // private var mediaConfig: MediaConfig? = null

    private var liver: ScreenLive? = null

    private var mediaProjectionManager: MediaProjectionManager? = null
    private var mediaProjection: MediaProjection? = null


    private lateinit var btnStartScreenLive: Button
    private lateinit var btnStopScreenLive: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
    }

    override fun onDestroy() {
        super.onDestroy()
        liver?.release()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            mediaProjection = data?.let {
                mediaProjectionManager?.getMediaProjection(resultCode, it)
            }

            if (null != mediaProjection) {
                liver = ScreenLive(RtmpPublisher.RTMP_URL, mediaProjection)
                liver?.start()
            }
        }
    }


    private fun initViews() {
        btnStartScreenLive = findViewById(R.id.btnScreenLiveStart)
        btnStartScreenLive.setOnClickListener(viewClicked)

        btnStopScreenLive = findViewById(R.id.btnScreenLiveStop)
        btnStopScreenLive.setOnClickListener(viewClicked)
    }


    private val viewClicked = View.OnClickListener { v ->
        if (v == btnStartScreenLive) {

            if (null == mediaProjectionManager) {
                mediaProjectionManager =
                    getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            }

            mediaProjectionManager?.let {
                val captureIntent = mediaProjectionManager!!.createScreenCaptureIntent()
                startActivityForResult(captureIntent, 100)
            }

        } else if (v == btnStopScreenLive) {
            liver?.stop()
        }
    }

}
