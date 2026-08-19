package app.streamlark.tv.ui

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import app.streamlark.tv.R
import app.streamlark.tv.data.FeedProviderRegistry
import app.streamlark.tv.data.FeedSession
import app.streamlark.tv.data.LibraryStore
import app.streamlark.tv.model.VideoItem
import kotlin.math.abs

/**
 * Phone-first short-video feed. The default provider is explicitly a public
 * demo feed; it does not claim to display or authenticate a third-party app.
 */
class ImmersiveFeedActivity : AppCompatActivity() {

    private lateinit var session: FeedSession
    private lateinit var libraryStore: LibraryStore
    private lateinit var playerView: PlayerView
    private lateinit var metadataPanel: TextView
    private lateinit var sourcePill: TextView
    private lateinit var sourceNotice: TextView
    private lateinit var favoriteAction: TextView
    private lateinit var errorOverlay: TextView
    private var player: ExoPlayer? = null
    private var currentVideo: VideoItem? = null
    private var touchStartY = 0f
    private var touchStartX = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableImmersiveChrome()
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
        enableImmersiveChrome()
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
                    togglePlayback()
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun enableImmersiveChrome() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }

    private fun buildScreen() {
        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
            keepScreenOn = true
        }
        playerView = PlayerView(this).apply {
            useController = false
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            setOnTouchListener(::handleFeedTouch)
        }
        root.addView(playerView, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ))
        root.addView(createTopBar())
        root.addView(createActionRail())

        metadataPanel = TextView(this).apply {
            textSize = 17f
            setLineSpacing(0f, 1.08f)
            setTextColor(Color.WHITE)
            setPadding(dp(18), dp(12), dp(86), dp(12))
            background = roundedDrawable(Color.argb(125, 8, 11, 19), dp(14))
        }
        root.addView(metadataPanel, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM or Gravity.START
        ).apply {
            marginStart = dp(16)
            marginEnd = dp(16)
            bottomMargin = dp(28)
        })

        sourceNotice = TextView(this).apply {
            textSize = 13f
            setTextColor(Color.WHITE)
            setPadding(dp(12), dp(8), dp(12), dp(8))
            background = roundedDrawable(Color.argb(180, 22, 27, 40), dp(10))
        }
        root.addView(sourceNotice, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.TOP or Gravity.CENTER_HORIZONTAL
        ).apply { topMargin = dp(72) })

        errorOverlay = TextView(this).apply {
            text = getString(R.string.player_error) + "\n轻触重试"
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(getColorCompat(R.color.lark_error))
            setPadding(dp(22), dp(18), dp(22), dp(18))
            background = roundedDrawable(Color.argb(225, 22, 27, 40), dp(16))
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

    private fun createTopBar(): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(16), dp(14), 0)
        }
        val back = iconAction("‹") { finish() }
        sourcePill = TextView(this).apply {
            textSize = 14f
            setTextColor(Color.WHITE)
            setPadding(dp(12), dp(8), dp(12), dp(8))
            background = roundedDrawable(Color.argb(155, 8, 11, 19), dp(12))
        }
        row.addView(back, LinearLayout.LayoutParams(dp(42), dp(42)).apply { marginEnd = dp(10) })
        row.addView(sourcePill, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ))
        return row
    }

    private fun createActionRail(): View {
        val rail = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }
        favoriteAction = iconAction("♡\n收藏") { toggleFavorite() }
        rail.addView(favoriteAction, railItemParams())
        rail.addView(iconAction("◉\n作者") { openProfile() }, railItemParams())
        rail.addView(iconAction("⋯\n详情") { openDetail() }, railItemParams())
        return rail.apply {
            layoutParams = FrameLayout.LayoutParams(dp(62), ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.END or Gravity.BOTTOM).apply {
                marginEnd = dp(20)
                bottomMargin = dp(142)
            }
        }
    }

    private fun railItemParams() = LinearLayout.LayoutParams(dp(62), dp(62)).apply {
        bottomMargin = dp(10)
    }

    private fun iconAction(label: String, onClick: () -> Unit): TextView = TextView(this).apply {
        text = label
        gravity = Gravity.CENTER
        textSize = 13f
        setTextColor(Color.WHITE)
        isFocusable = true
        isClickable = true
        setPadding(dp(4), dp(4), dp(4), dp(4))
        background = roundedDrawable(Color.argb(140, 8, 11, 19), dp(18))
        setOnClickListener { onClick() }
        onFocusChangeListener = View.OnFocusChangeListener { view, focused ->
            view.background = roundedDrawable(
                Color.argb(if (focused) 205 else 140, 8, 11, 19), dp(18),
                if (focused) getColorCompat(R.color.lark_accent) else Color.TRANSPARENT,
                if (focused) dp(2) else 0
            )
        }
    }

    private fun handleFeedTouch(view: View, event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchStartY = event.rawY
                touchStartX = event.rawX
                return true
            }
            MotionEvent.ACTION_UP -> {
                val deltaY = event.rawY - touchStartY
                val deltaX = event.rawX - touchStartX
                when {
                    abs(deltaY) > SWIPE_THRESHOLD_PX && abs(deltaY) > abs(deltaX) -> {
                        moveInFeed(if (deltaY < 0) 1 else -1)
                    }
                    abs(deltaX) > SWIPE_THRESHOLD_PX -> {
                        seekBy(if (deltaX < 0) SEEK_AMOUNT_MS else -SEEK_AMOUNT_MS)
                    }
                    else -> togglePlayback()
                }
                return true
            }
        }
        return true
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
                if (playbackState == Player.STATE_ENDED) showMessage("推荐流已播放完毕")
            }
        })
        rebuildPlaylist(
            startPositionMs = currentVideo?.let { libraryStore.progressFor(it.id) } ?: 0L,
            autoPlay = true
        )
    }

    private fun rebuildPlaylist(startPositionMs: Long, autoPlay: Boolean) {
        val activePlayer = player ?: return
        activePlayer.setMediaItems(session.all().map { item ->
            MediaItem.Builder().setMediaId(item.id).setUri(item.sourceUrl).build()
        }, session.currentPosition, startPositionMs)
        activePlayer.prepare()
        activePlayer.playWhenReady = autoPlay
        errorOverlay.visibility = View.GONE
        updateMetadata()
    }

    private fun moveInFeed(offset: Int) {
        val before = currentVideo ?: return
        val next = session.moveBy(offset) ?: return
        if (next.id == before.id) {
            showMessage(if (offset > 0) "已到演示流末尾" else "已经是第一条")
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

    private fun togglePlayback() {
        player?.let { activePlayer ->
            activePlayer.playWhenReady = !activePlayer.playWhenReady
            showMessage(if (activePlayer.playWhenReady) "播放" else "暂停")
        }
    }

    private fun seekBy(amountMs: Long) {
        player?.let { activePlayer ->
            val duration = activePlayer.duration.takeIf { it > 0L } ?: Long.MAX_VALUE
            activePlayer.seekTo((activePlayer.currentPosition + amountMs).coerceIn(0L, duration))
        }
    }

    private fun toggleFavorite() {
        val item = currentVideo ?: return
        val favorited = libraryStore.toggleFavorite(item.id)
        favoriteAction.text = if (favorited) "♥\n已收藏" else "♡\n收藏"
        showMessage(if (favorited) "已收藏" else "已取消收藏")
    }

    private fun openProfile() {
        val item = currentVideo ?: return
        val profile = FeedProviderRegistry.active().profileFor(item) ?: return
        startActivity(Intent(this, ProfileActivity::class.java).putExtra(ProfileActivity.EXTRA_PROFILE_ID, profile.id))
    }

    private fun openDetail() {
        val item = currentVideo ?: return
        startActivity(Intent(this, DetailActivity::class.java).putExtra(DetailActivity.EXTRA_VIDEO_ID, item.id))
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
        val provider = FeedProviderRegistry.active()
        val profile = provider.profileFor(item)
        metadataPanel.text = "${profile?.handle ?: item.creator}\n${item.title}\n${item.description}"
        sourcePill.text = "${session.currentPosition + 1}/${session.size} · ${provider.displayName}"
        sourceNotice.text = if (provider.id == "demo") {
            "演示媒体：尚未配置授权内容源"
        } else {
            "当前内容来自已配置授权 Provider"
        }
        favoriteAction.text = if (libraryStore.isFavorite(item.id)) "♥\n已收藏" else "♡\n收藏"
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_INITIAL_VIDEO_ID = "extra_initial_video_id"
        private const val SEEK_AMOUNT_MS = 10_000L
        private const val SWIPE_THRESHOLD_PX = 96f
    }
}
