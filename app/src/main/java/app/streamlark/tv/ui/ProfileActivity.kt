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
import app.streamlark.tv.model.CreatorProfile
import app.streamlark.tv.model.VideoItem

class ProfileActivity : AppCompatActivity() {

    private lateinit var profile: CreatorProfile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profile = FeedProviderRegistry.active().profile(intent.getStringExtra(EXTRA_PROFILE_ID)) ?: run {
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
            setPadding(dp(56), dp(42), dp(56), dp(52))
        }
        root.addView(body)

        body.addView(createBackButton())
        body.addView(createProfileHeader(), LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(214)
        ).apply { topMargin = dp(24) })
        body.addView(createSectionTitle("作品"), sectionParams())
        val worksRow = createVideoRow().also { row ->
            row.adapter = ContentCardAdapter(::openDetail).also {
                it.submitList(FeedProviderRegistry.active().works(profile.id))
            }
        }
        body.addView(worksRow, rowParams())

        body.addView(createSectionTitle("合集"), sectionParams())
        val collectionsRow = createCollectionRow().also { row ->
            row.adapter = CollectionCardAdapter(::openCollection).also {
                it.submitList(FeedProviderRegistry.active().collectionsFor(profile.id))
            }
        }
        body.addView(collectionsRow, collectionRowParams())
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
        onFocusChangeListener = focusListener(this, false)
    }

    private fun createProfileHeader(): View {
        val header = FrameLayout(this).apply {
            background = roundedDrawable(Color.parseColor(profile.accentColorHex), dp(18))
        }
        val avatar = TextView(this).apply {
            text = profile.displayName.take(1)
            textSize = 40f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
            background = roundedDrawable(Color.argb(110, 0, 0, 0), dp(44))
        }
        header.addView(avatar, FrameLayout.LayoutParams(dp(88), dp(88), Gravity.TOP or Gravity.START).apply {
            topMargin = dp(28)
            marginStart = dp(28)
        })
        val textColumn = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        textColumn.addView(TextView(this).apply {
            text = profile.displayName
            textSize = 26f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.WHITE)
        })
        textColumn.addView(TextView(this).apply {
            text = "${profile.handle} · ${profile.followerLabel}"
            textSize = 15f
            setTextColor(Color.argb(220, 255, 255, 255))
            setPadding(0, dp(5), 0, 0)
        })
        textColumn.addView(TextView(this).apply {
            text = profile.bio
            textSize = 16f
            maxLines = 2
            setTextColor(Color.WHITE)
            setPadding(0, dp(16), 0, 0)
        })
        header.addView(textColumn, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL
        ).apply {
            marginStart = dp(136)
            marginEnd = dp(28)
        })
        return header
    }

    private fun createSectionTitle(text: String): TextView = TextView(this).apply {
        this.text = text
        textSize = 22f
        setTypeface(typeface, Typeface.BOLD)
        setTextColor(getColorCompat(R.color.lark_text_primary))
    }

    private fun createVideoRow(): RecyclerView = RecyclerView(this).apply {
        layoutManager = LinearLayoutManager(this@ProfileActivity, RecyclerView.HORIZONTAL, false)
        overScrollMode = View.OVER_SCROLL_NEVER
        clipToPadding = false
        setPadding(0, 0, dp(24), 0)
    }

    private fun createCollectionRow(): RecyclerView = RecyclerView(this).apply {
        layoutManager = LinearLayoutManager(this@ProfileActivity, RecyclerView.HORIZONTAL, false)
        overScrollMode = View.OVER_SCROLL_NEVER
        clipToPadding = false
        setPadding(0, 0, dp(24), 0)
    }

    private fun sectionParams() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply {
        topMargin = dp(32)
        bottomMargin = dp(4)
    }

    private fun rowParams() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, dp(236)
    )

    private fun collectionRowParams() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, dp(182)
    )

    private fun focusListener(view: TextView, primary: Boolean) = View.OnFocusChangeListener { _, focused ->
        view.background = roundedDrawable(
            if (primary) getColorCompat(R.color.lark_accent) else getColorCompat(R.color.lark_surface_elevated),
            dp(10),
            if (focused) getColorCompat(R.color.lark_focus) else Color.TRANSPARENT,
            if (focused) dp(2) else 0
        )
    }

    private fun openDetail(video: VideoItem) {
        startActivity(Intent(this, DetailActivity::class.java).putExtra(DetailActivity.EXTRA_VIDEO_ID, video.id))
    }

    private fun openCollection(collection: app.streamlark.tv.model.ContentCollection) {
        startActivity(Intent(this, CollectionActivity::class.java)
            .putExtra(CollectionActivity.EXTRA_COLLECTION_ID, collection.id))
    }

    companion object {
        const val EXTRA_PROFILE_ID = "extra_profile_id"
    }
}
