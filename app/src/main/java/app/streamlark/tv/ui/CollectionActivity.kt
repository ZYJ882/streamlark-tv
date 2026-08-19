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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.streamlark.tv.R
import app.streamlark.tv.data.FeedProviderRegistry
import app.streamlark.tv.model.ContentCollection
import app.streamlark.tv.model.VideoItem

class CollectionActivity : AppCompatActivity() {

    private lateinit var collection: ContentCollection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        collection = FeedProviderRegistry.active().collection(intent.getStringExtra(EXTRA_COLLECTION_ID)) ?: run {
            finish()
            return
        }
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
        body.addView(createHeader(), LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(224)
        ).apply { topMargin = dp(24) })
        body.addView(createSectionTitle("合集内容"), sectionParams())
        val row = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@CollectionActivity, RecyclerView.HORIZONTAL, false)
            overScrollMode = View.OVER_SCROLL_NEVER
            clipToPadding = false
            setPadding(0, 0, dp(24), 0)
            adapter = ContentCardAdapter(::openDetail).also {
                it.submitList(FeedProviderRegistry.active().collectionVideos(collection.id))
            }
        }
        body.addView(row, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(236)))
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

    private fun createHeader(): View {
        val header = FrameLayout(this).apply {
            background = roundedDrawable(Color.parseColor(collection.accentColorHex), dp(18))
        }
        val textColumn = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        textColumn.addView(TextView(this).apply {
            text = "合集 · ${collection.videoIds.size} 条"
            textSize = 15f
            setTextColor(Color.argb(220, 255, 255, 255))
        })
        textColumn.addView(TextView(this).apply {
            text = collection.title
            textSize = 30f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setPadding(0, dp(10), 0, 0)
        })
        textColumn.addView(TextView(this).apply {
            text = collection.subtitle
            textSize = 17f
            maxLines = 2
            setTextColor(Color.WHITE)
            setPadding(0, dp(12), 0, 0)
        })
        val firstVideo = FeedProviderRegistry.active().collectionVideos(collection.id).firstOrNull()
        if (firstVideo != null) {
            textColumn.addView(createPlayButton(firstVideo), LinearLayout.LayoutParams(
                dp(152), dp(46)
            ).apply { topMargin = dp(18) })
        }
        header.addView(textColumn, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL
        ).apply {
            marginStart = dp(30)
            marginEnd = dp(30)
        })
        return header
    }

    private fun createPlayButton(firstVideo: VideoItem): TextView = TextView(this).apply {
        text = "▶  播放第一条"
        textSize = 15f
        gravity = Gravity.CENTER
        isFocusable = true
        setTextColor(getColorCompat(R.color.lark_background))
        background = roundedDrawable(getColorCompat(R.color.lark_accent), dp(12))
        setOnClickListener {
            startActivity(Intent(this@CollectionActivity, PlayerActivity::class.java)
                .putExtra(PlayerActivity.EXTRA_VIDEO_ID, firstVideo.id))
        }
        onFocusChangeListener = View.OnFocusChangeListener { view, focused ->
            view.background = roundedDrawable(
                getColorCompat(R.color.lark_accent), dp(12),
                if (focused) getColorCompat(R.color.lark_focus) else Color.TRANSPARENT,
                if (focused) dp(2) else 0
            )
        }
    }

    private fun createSectionTitle(text: String): TextView = TextView(this).apply {
        this.text = text
        textSize = 22f
        setTypeface(typeface, Typeface.BOLD)
        setTextColor(getColorCompat(R.color.lark_text_primary))
    }

    private fun sectionParams() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply {
        topMargin = dp(32)
        bottomMargin = dp(4)
    }

    private fun openDetail(video: VideoItem) {
        startActivity(Intent(this, DetailActivity::class.java).putExtra(DetailActivity.EXTRA_VIDEO_ID, video.id))
    }

    companion object {
        const val EXTRA_COLLECTION_ID = "extra_collection_id"
    }
}
