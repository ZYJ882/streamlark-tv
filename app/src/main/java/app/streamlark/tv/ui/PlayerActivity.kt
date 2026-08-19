package app.streamlark.tv.ui

import android.annotation.TargetApi
import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import app.streamlark.tv.R
import app.streamlark.tv.data.FeedProviderRegistry
import app.streamlark.tv.data.LibraryStore
import app.streamlark.tv.model.VideoItem

class PlayerActivity : AppCompatActivity() {

    private lateinit var libraryStore: LibraryStore
    private lateinit var playerView: PlayerView
    private lateinit var titleOverlay: TextView
    private lateinit var errorOverlay: TextView
    private var player: ExoPlayer? = null
    private var video: VideoItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        video = FeedProviderRegistry.active().loadInitial()
            .firstOrNull { it.id == intent.getStringExtra(EXTRA_VIDEO_ID) }
        if (video == null) {
            finish()
            return
        }
        libraryStore = LibraryStore(this)
        buildScreen()
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) initializePlayer()
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) initializePlayer()
    }

    override fun onPause() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) releasePlayer()
        super.onPause()
    }

    override fun onStop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !isInPictureInPictureCompat()) releasePlayer()
        super.onStop()
    }

    override fun onUserLeaveHint() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && player?.isPlaying == true) {
            PictureInPictureHelper.enter(this)
        }
        super.onUserLeaveHint()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    seekBy(10_000L)
                    return true
                }
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    seekBy(-10_000L)
                    return true
                }
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    changeVideo(1)
                    return true
                }
                KeyEvent.KEYCODE_DPAD_UP -> {
                    changeVideo(-1)
                    return true
                }
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
                KeyEvent.KEYCODE_DPAD_CENTER,
                KeyEvent.KEYCODE_ENTER -> {
                    player?.let { activePlayer ->
                        activePlayer.playWhenReady = !activePlayer.playWhenReady
                        showTransientTitle(if (activePlayer.playWhenReady) "播放" else "暂停")
                    }
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun buildScreen() {
        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
            keepScreenOn = true
        }
        playerView = PlayerView(this).apply {
            useController = true
            controllerShowTimeoutMs = 3_000
            setShowNextButton(false)
            setShowPreviousButton(false)
            setShowFastForwardButton(false)
            setShowRewindButton(false)
        }
        root.addView(playerView, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ))

        titleOverlay = TextView(this).apply {
            text = video?.title.orEmpty()
            textSize = 18f
            setTextColor(Color.WHITE)
            setPadding(dp(18), dp(12), dp(18), dp(12))
            background = roundedDrawable(Color.argb(180, 16, 19, 27), dp(12))
        }
        root.addView(titleOverlay, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.TOP or Gravity.START
        ).apply {
            topMargin = dp(28)
            marginStart = dp(32)
        })

        errorOverlay = TextView(this).apply {
            text = getString(R.string.player_error)
            textSize = 17f
            gravity = Gravity.CENTER
            setTextColor(getColorCompat(R.color.lark_error))
            setPadding(dp(20), dp(14), dp(20), dp(14))
            background = roundedDrawable(Color.argb(220, 26, 32, 48), dp(14))
            visibility = View.GONE
            isFocusable = true
            setOnClickListener { restartCurrentVideo() }
        }
        root.addView(errorOverlay, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        ))

        val hint = TextView(this).apply {
            text = "←/→ 快退快进  ·  ↑/↓ 切换内容  ·  确认 暂停/播放"
            textSize = 14f
            setTextColor(Color.WHITE)
            setPadding(dp(14), dp(8), dp(14), dp(8))
            background = roundedDrawable(Color.argb(150, 16, 19, 27), dp(10))
        }
        root.addView(hint, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        ).apply { bottomMargin = dp(24) })
        setContentView(root)
    }

    private fun initializePlayer() {
        val currentVideo = video ?: return
        if (player != null) return
        errorOverlay.visibility = View.GONE
        player = ExoPlayer.Builder(this).build().also { activePlayer ->
            playerView.player = activePlayer
            activePlayer.setMediaItem(MediaItem.fromUri(currentVideo.sourceUrl))
            activePlayer.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    errorOverlay.visibility = View.VISIBLE
                    errorOverlay.requestFocus()
                }
            })
            activePlayer.prepare()
            val savedPosition = libraryStore.progressFor(currentVideo.id)
            if (savedPosition > 0L) activePlayer.seekTo(savedPosition)
            activePlayer.playWhenReady = true
        }
    }

    private fun releasePlayer() {
        player?.let { activePlayer ->
            video?.let { item -> libraryStore.saveProgress(item.id, activePlayer.currentPosition) }
            playerView.player = null
            activePlayer.release()
        }
        player = null
    }

    private fun seekBy(amountMs: Long) {
        player?.let { activePlayer ->
            val duration = activePlayer.duration.takeIf { it > 0L } ?: Long.MAX_VALUE
            val nextPosition = (activePlayer.currentPosition + amountMs).coerceIn(0L, duration)
            activePlayer.seekTo(nextPosition)
            showTransientTitle(if (amountMs > 0) "快进 10 秒" else "快退 10 秒")
        }
    }

    private fun changeVideo(offset: Int) {
        val current = video ?: return
        val all = FeedProviderRegistry.active().loadInitial()
        val currentIndex = all.indexOfFirst { it.id == current.id }
        if (currentIndex < 0) return
        releasePlayer()
        video = all[(currentIndex + offset + all.size) % all.size]
        titleOverlay.text = video?.title.orEmpty()
        showTransientTitle(if (offset > 0) "下一条" else "上一条")
        initializePlayer()
    }

    private fun restartCurrentVideo() {
        releasePlayer()
        initializePlayer()
    }

    private fun showTransientTitle(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun isInPictureInPictureCompat(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && PictureInPictureHelper.isInPictureMode(this)

    private object PictureInPictureHelper {
        @TargetApi(Build.VERSION_CODES.O)
        fun enter(activity: Activity) {
            val params = android.app.PictureInPictureParams.Builder()
                .setAspectRatio(android.util.Rational(16, 9))
                .build()
            activity.enterPictureInPictureMode(params)
        }

        @TargetApi(Build.VERSION_CODES.N)
        fun isInPictureMode(activity: Activity): Boolean = activity.isInPictureInPictureMode
    }

    companion object {
        const val EXTRA_VIDEO_ID = "extra_video_id"
    }
}
