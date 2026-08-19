package app.streamlark.tv.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Keeps device routing out of the two presentation activities so both shells
 * can reuse the same provider, library and playback layers.
 */
class LaunchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val destination = when (DeviceUiRouter.current(this)) {
            DeviceUiMode.MOBILE -> ImmersiveFeedActivity::class.java
            DeviceUiMode.TELEVISION -> MainActivity::class.java
        }
        startActivity(Intent(this, destination).apply {
            putExtras(intent)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        })
        finish()
    }
}
