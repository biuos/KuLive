package dai.android.media.live

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private var mediaConfig: MediaConfig? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Example of a call to a native method
        findViewById<TextView>(R.id.sample_text).text = stringFromJNI()

        mediaConfig = MediaConfig.Builder("rtmp://xxx")
            .setAudio(
                MediaConfig.AudioBuilder().build()
            )
            .setVideo(
                MediaConfig.VideoBuilder().build()
            ).build()

        mediaConfig!!.audio.channelConfig

    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}