package app.streamlark.tv.ui

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
import app.streamlark.tv.data.FeedSession
import app.streamlark.tv.data.LibraryStore
import app.streamlark.tv.model.VideoItem

/**
 * A remote-first short-video feed. It queues the current feed in one Media3
 * player so the following item is ready for transition without creating a new
 * player instance for every D-pad action.
 */
class ImmersiveFeedActivity : AppCompatActivity() {

    private lateinit var session: FeedSession
    private lateinit var libraryStore: LibraryStore
    private lateinit var playerView: PlayerView
    private lateinit var metadataPanel: TextView
    private lateinit var positionPanel: TextView
    private lateinit var errorOverlay: TextView
    private var player: ExoPlayer? = null
    private var currentVideo: VideoItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        libraryStore = LibraryStore(this)
        session = FeedSession(
            provider = FeedProviderRegistry.active(),
            initialVideoId = intent.getStringExtra(EXTRA_INITIAL_VIDEO_ID)
        )
        currentVideo = session.current
        if (currentVideo == null) {
            finish()
            return
        }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) releasePlayer()
        super.onStop()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    moveInFeed(1)
                    return true
                }
                KeyEvent.KEYCODE_DPAD_UP -> {
                    moveInFeed(-1)
                    return true
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    seekBy(SEEK_AMOUNT_MS)
                    return true
                }
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    seekBy(-SEEK_AMOUNT_MS)
                    return true
                }
                KeyEvent.KEYCODE_DPAD_CENTER,
                KeyEvent.KEYCODE_ENTER,
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                    player?.let { activePlayer ->
                        activePlayer.playWhenReady = !activePlayer.playWhenReady
                        showMessage(if (activePlayer.playWhenReady) "播放" else "暂停")
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
            controllerShowTimeoutMs = 2_500
            setShowNextButton(false)
            setShowPreviousButton(false)
            setShowFastForwardButton(false)
            setShowRewindButton(false)
        }
        root.addView(playerView, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ))

        positionPanel = TextView(this).apply {
            textSize = 14f
            setTextColor(Color.WHITE)
            setPadding(dp(12), dp(7), dp(12), dp(7))
            background = roundedDrawable(Color.argb(175, 16, 19, 27), dp(10))
        }
        root.addView(positionPanel, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.TOP or Gravity.END
        ).apply {
            topMargin = dp(28)
            marginEnd = dp(32)
        })

        metadataPanel = TextView(this).apply {
            textSize = 17f
            setTextColor(Color.WHITE)
            setLineSpacing(0f, 1.12f)
            setPadding(dp(18), dp(14), dp(18), dp(14))
            background = roundedDrawable(Color.argb(195, 16, 19, 27), dp(14))
        }
        root.addView(metadataPanel, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM or Gravity.START
        ).apply {
            marginStart = dp(34)
            marginEnd = dp(34)
            bottomMargin = dp(34)
        })

        errorOverlay = TextView(this).apply {
            text = getString(R.string.player_error) + "\n按确认键重试"
            textSize = 17f
            gravity = Gravity.CENTER
            setTextColor(getColorCompat(R.color.lark_error))
            setPadding(dp(20), dp(16), dp(20), dp(16))
            background = roundedDrawable(Color.argb(220, 26, 32, 48), dp(14))
            visibility = View.GONE
            isFocusable = true
            setOnClickListener { restartFeed() }
        }
        root.addView(errorOverlay, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        ))
        setContentView(root)
        updateMetadata()
    }

    private fun initializePlayer() {
        if (player != null) return
        val activePlayer = ExoPlayer.Builder(this).build()
        player = activePlayer
        playerView.player = activePlayer
        activePlayer.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                errorOverlay.visibility = View.VISIBLE
                errorOverlay.requestFocus()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val item = mediaItem?.mediaId?.let(session::moveTo) ?: return
                currentVideo = item
                updateMetadata()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    showMessage("推荐流已经播放完毕")
                }
            }
        })
        rebuildPlaylist(
            startPositionMs = currentVideo?.let { libraryStore.progressFor(it.id) } ?: 0L,
            autoPlay = true
        )
    }

    private fun rebuildPlaylist(startPositionMs: Long, autoPlay: Boolean) {
        val activePlayer = player ?: return
        val mediaItems = session.all().map { item ->
            MediaItem.Builder()
                .setMediaId(item.id)
                .setUri(item.sourceUrl)
                .build()
        }
        activePlayer.setMediaItems(mediaItems, session.currentPosition, startPositionMs)
        activePlayer.prepare()
        activePlayer.playWhenReady = autoPlay
        errorOverlay.visibility = View.GONE
        updateMetadata()
    }

    private fun moveInFeed(offset: Int) {
        val before = currentVideo ?: return
        val next = session.moveBy(offset) ?: return
        if (next.id == before.id) {
            showMessage(if (offset > 0) "已到推荐流尾部" else "已经是第一条")
            return
        }
        saveCurrentProgress()
        currentVideo = next
        val activePlayer = player ?: return
        if (activePlayer.mediaItemCount < session.size) {
            rebuildPlaylist(libraryStore.progressFor(next.id), true)
        } else {
            activePlayer.seekTo(session.currentPosition, libraryStore.progressFor(next.id))
            activePlayer.playWhenReady = true
            errorOverlay.visibility = View.GONE
            updateMetadata()
        }
    }

    private fun seekBy(amountMs: Long) {
        player?.let { activePlayer ->
            val duration = activePlayer.duration.takeIf { it > 0L } ?: Long.MAX_VALUE
            activePlayer.seekTo((activePlayer.currentPosition + amountMs).coerceIn(0L, duration))
            showMessage(if (amountMs > 0L) "快进 10 秒" else "快退 10 秒")
        }
    }

    private fun restartFeed() {
        errorOverlay.visibility = View.GONE
        releasePlayer()
        initializePlayer()
    }

    private fun saveCurrentProgress() {
        player?.let { activePlayer ->
            currentVideo?.let { item -> libraryStore.saveProgress(item.id, activePlayer.currentPosition) }
        }
    }

    private fun releasePlayer() {
        saveCurrentProgress()
        player?.release()
        playerView.player = null
        player = null
    }

    private fun updateMetadata() {
        val item = currentVideo ?: return
        metadataPanel.text = "${item.title}\n${item.creator} · ${item.category.label} · ${item.durationLabel}\n↑/↓ 刷新一条  ·  ←/→ 快退快进  ·  确认 暂停/播放"
        positionPanel.text = "${FeedProviderRegistry.active().displayName}  ${session.currentPosition + 1}/${session.size}"
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_INITIAL_VIDEO_ID = "extra_initial_video_id"
        private const val SEEK_AMOUNT_MS = 10_000L
    }
}
