package app.streamlark.tv.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import app.streamlark.tv.R
import app.streamlark.tv.data.DemoCatalog
import app.streamlark.tv.data.LibraryStore
import app.streamlark.tv.model.VideoItem

class DetailActivity : AppCompatActivity() {

    private lateinit var video: VideoItem
    private lateinit var libraryStore: LibraryStore
    private lateinit var favoriteButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        video = DemoCatalog.find(intent.getStringExtra(EXTRA_VIDEO_ID)) ?: run {
            finish()
            return
        }
        libraryStore = LibraryStore(this)
        render()
    }

    private fun render() {
        val root = ScrollView(this).apply {
            setBackgroundColor(getColorCompat(R.color.lark_background))
            isFillViewport = true
        }
        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(56), dp(42), dp(56), dp(48))
        }
        root.addView(body)

        body.addView(createBackButton())
        body.addView(createHero(), LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(278)
        ).apply { topMargin = dp(24) })
        body.addView(createTitle(), LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(26) })
        body.addView(createMeta(), LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(8) })
        body.addView(createDescription(), LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(18) })

        val actionRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        actionRow.addView(createActionButton(getString(R.string.play), true) { openPlayer() },
            LinearLayout.LayoutParams(dp(170), dp(52)).apply { marginEnd = dp(14) })
        favoriteButton = createActionButton("", false) { toggleFavorite() }
        actionRow.addView(favoriteButton, LinearLayout.LayoutParams(dp(170), dp(52)))
        body.addView(actionRow, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(28) })

        body.addView(createProgressHint(), LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(18) })
        body.addView(createProviderNotice(), LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(22) })

        setContentView(root)
        updateFavoriteLabel()
    }

    private fun createBackButton(): TextView = TextView(this).apply {
        text = "‹  ${getString(R.string.back)}"
        textSize = 18f
        setTextColor(getColorCompat(R.color.lark_text_primary))
        gravity = Gravity.CENTER_VERTICAL
        isFocusable = true
        setPadding(dp(12), dp(8), dp(12), dp(8))
        background = roundedDrawable(getColorCompat(R.color.lark_surface), dp(10))
        setOnClickListener { finish() }
        onFocusChangeListener = actionFocusListener(this, false)
    }

    private fun createHero(): View {
        val hero = FrameLayout(this).apply {
            background = roundedDrawable(Color.parseColor(video.accentColorHex), dp(18))
        }
        hero.addView(TextView(this).apply {
            text = video.badge
            textSize = 15f
            setTextColor(Color.WHITE)
            setPadding(dp(14), dp(7), dp(14), dp(7))
            background = roundedDrawable(Color.argb(160, 16, 19, 27), dp(12))
        }, FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.START).apply {
            topMargin = dp(20)
            marginStart = dp(20)
        })
        hero.addView(TextView(this).apply {
            text = "▶"
            textSize = 62f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = roundedDrawable(Color.argb(100, 0, 0, 0), dp(44))
            isFocusable = true
            contentDescription = getString(R.string.play)
            setOnClickListener { openPlayer() }
            onFocusChangeListener = View.OnFocusChangeListener { view, focused ->
                view.animate().scaleX(if (focused) 1.12f else 1f).scaleY(if (focused) 1.12f else 1f)
                    .setDuration(130L).start()
            }
        }, FrameLayout.LayoutParams(dp(88), dp(88), Gravity.CENTER))
        return hero
    }

    private fun createTitle(): TextView = TextView(this).apply {
        text = video.title
        textSize = 30f
        setTypeface(typeface, Typeface.BOLD)
        setTextColor(getColorCompat(R.color.lark_text_primary))
    }

    private fun createMeta(): TextView = TextView(this).apply {
        text = "${video.creator} · ${video.category.label} · ${video.durationLabel}"
        textSize = 17f
        setTextColor(getColorCompat(R.color.lark_text_secondary))
    }

    private fun createDescription(): TextView = TextView(this).apply {
        text = video.description
        textSize = 18f
        setLineSpacing(0f, 1.15f)
        setTextColor(getColorCompat(R.color.lark_text_primary))
    }

    private fun createActionButton(label: String, primary: Boolean, onClick: () -> Unit): TextView {
        return TextView(this).apply {
            text = label
            textSize = 17f
            gravity = Gravity.CENTER
            isFocusable = true
            setTextColor(if (primary) getColorCompat(R.color.lark_background) else getColorCompat(R.color.lark_text_primary))
            background = roundedDrawable(
                if (primary) getColorCompat(R.color.lark_accent) else getColorCompat(R.color.lark_surface),
                dp(12)
            )
            setOnClickListener { onClick() }
            onFocusChangeListener = actionFocusListener(this, primary)
        }
    }

    private fun actionFocusListener(view: TextView, primary: Boolean) = View.OnFocusChangeListener { _, focused ->
        view.background = roundedDrawable(
            if (primary) getColorCompat(R.color.lark_accent) else getColorCompat(R.color.lark_surface_elevated),
            dp(12),
            if (focused) getColorCompat(R.color.lark_focus) else Color.TRANSPARENT,
            if (focused) dp(2) else 0
        )
        view.animate().scaleX(if (focused) 1.04f else 1f).scaleY(if (focused) 1.04f else 1f)
            .setDuration(120L).start()
    }

    private fun createProgressHint(): TextView {
        val savedPosition = libraryStore.progressFor(video.id)
        val text = if (savedPosition > 0) {
            "已保存播放进度：${formatTime(savedPosition)}。点击“播放”将从该位置继续。"
        } else {
            "尚无播放记录。首次播放后将自动保存在此设备。"
        }
        return TextView(this).apply {
            this.text = text
            textSize = 15f
            setTextColor(getColorCompat(R.color.lark_text_secondary))
        }
    }

    private fun createProviderNotice(): TextView = TextView(this).apply {
        text = "内容来源：StreamLark 本地演示 Provider。首版不请求或保存第三方平台 Cookie。"
        textSize = 14f
        setTextColor(getColorCompat(R.color.lark_text_secondary))
        setPadding(dp(16), dp(14), dp(16), dp(14))
        background = roundedDrawable(getColorCompat(R.color.lark_surface), dp(12))
    }

    private fun updateFavoriteLabel() {
        favoriteButton.text = if (libraryStore.isFavorite(video.id)) {
            "★ ${getString(R.string.favorited)}"
        } else {
            "☆ ${getString(R.string.favorite)}"
        }
    }

    private fun toggleFavorite() {
        libraryStore.toggleFavorite(video.id)
        updateFavoriteLabel()
    }

    private fun openPlayer() {
        startActivity(Intent(this, PlayerActivity::class.java).putExtra(PlayerActivity.EXTRA_VIDEO_ID, video.id))
    }

    private fun formatTime(positionMs: Long): String {
        val seconds = positionMs / 1000
        return "%d:%02d".format(seconds / 60, seconds % 60)
    }

    companion object {
        const val EXTRA_VIDEO_ID = "extra_video_id"
    }
}
