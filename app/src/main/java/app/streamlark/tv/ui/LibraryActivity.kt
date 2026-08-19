package app.streamlark.tv.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.streamlark.tv.R
import app.streamlark.tv.data.FeedProviderRegistry
import app.streamlark.tv.data.LibraryStore
import app.streamlark.tv.model.VideoItem

class LibraryActivity : AppCompatActivity() {

    private lateinit var libraryStore: LibraryStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        libraryStore = LibraryStore(this)
        render()
    }

    override fun onResume() {
        super.onResume()
        if (::libraryStore.isInitialized) render()
    }

    private fun render() {
        val catalog = FeedProviderRegistry.active().loadInitial()
        val favorites = libraryStore.favoriteItems(catalog)
        val history = libraryStore.recent(catalog)

        val root = ScrollView(this).apply {
            setBackgroundColor(getColorCompat(R.color.lark_background))
            isFillViewport = true
        }
        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(56), dp(42), dp(56), dp(52))
        }
        root.addView(body)
        body.addView(createBackButton())
        body.addView(createTitle(), LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(28) })
        body.addView(createSummary(favorites.size, history.size), LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(10) })

        body.addView(createSectionTitle("我的收藏"), sectionParams())
        body.addView(createContentOrEmpty(favorites, "尚未收藏内容。在详情页按“收藏”即可保存到此设备。"))
        body.addView(createSectionTitle("观看历史"), sectionParams())
        body.addView(createContentOrEmpty(history, "尚无观看历史。播放任意演示内容后会自动保存进度。"))
        setContentView(root)
    }

    private fun createBackButton(): TextView = TextView(this).apply {
        text = "‹  ${getString(R.string.back)}"
        textSize = 18f
        gravity = Gravity.CENTER_VERTICAL
        setTextColor(getColorCompat(R.color.lark_text_primary))
        setPadding(dp(12), dp(8), dp(12), dp(8))
        isFocusable = true
        background = roundedDrawable(getColorCompat(R.color.lark_surface), dp(10))
        setOnClickListener { finish() }
        onFocusChangeListener = View.OnFocusChangeListener { view, focused ->
            view.background = roundedDrawable(
                getColorCompat(R.color.lark_surface_elevated), dp(10),
                if (focused) getColorCompat(R.color.lark_focus) else Color.TRANSPARENT,
                if (focused) dp(2) else 0
            )
        }
    }

    private fun createTitle(): TextView = TextView(this).apply {
        text = "我的资料库"
        textSize = 30f
        setTypeface(typeface, Typeface.BOLD)
        setTextColor(getColorCompat(R.color.lark_text_primary))
    }

    private fun createSummary(favoriteCount: Int, historyCount: Int): TextView = TextView(this).apply {
        text = "本设备已保存 $favoriteCount 个收藏 · $historyCount 条观看记录"
        textSize = 16f
        setTextColor(getColorCompat(R.color.lark_text_secondary))
        setPadding(dp(16), dp(14), dp(16), dp(14))
        background = roundedDrawable(getColorCompat(R.color.lark_surface), dp(12))
    }

    private fun createSectionTitle(text: String): TextView = TextView(this).apply {
        this.text = text
        textSize = 22f
        setTypeface(typeface, Typeface.BOLD)
        setTextColor(getColorCompat(R.color.lark_text_primary))
    }

    private fun createContentOrEmpty(items: List<VideoItem>, emptyText: String): View {
        if (items.isEmpty()) {
            return TextView(this).apply {
                text = emptyText
                textSize = 16f
                setTextColor(getColorCompat(R.color.lark_text_secondary))
                setPadding(dp(16), dp(18), dp(16), dp(18))
                background = roundedDrawable(getColorCompat(R.color.lark_surface), dp(12))
            }
        }
        return RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@LibraryActivity, RecyclerView.HORIZONTAL, false)
            overScrollMode = View.OVER_SCROLL_NEVER
            clipToPadding = false
            setPadding(0, 0, dp(24), 0)
            adapter = ContentCardAdapter(::openDetail).also { it.submitList(items) }
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(236))
        }
    }

    private fun sectionParams() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply {
        topMargin = dp(30)
        bottomMargin = dp(4)
    }

    private fun openDetail(video: VideoItem) {
        startActivity(Intent(this, DetailActivity::class.java).putExtra(DetailActivity.EXTRA_VIDEO_ID, video.id))
    }
}
