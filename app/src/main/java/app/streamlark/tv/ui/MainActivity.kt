package app.streamlark.tv.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.streamlark.tv.R
import app.streamlark.tv.auth.DouyinAuthStore
import app.streamlark.tv.data.FeedProviderRegistry
import app.streamlark.tv.data.LibraryStore
import app.streamlark.tv.model.ContentCategory
import app.streamlark.tv.model.VideoItem

class MainActivity : AppCompatActivity() {

    private lateinit var libraryStore: LibraryStore
    private lateinit var douyinAuthStore: DouyinAuthStore
    private lateinit var featuredAdapter: ContentCardAdapter
    private lateinit var recentAdapter: ContentCardAdapter
    private lateinit var contentAdapter: ContentCardAdapter
    private lateinit var recentSection: LinearLayout
    private lateinit var contentHeading: TextView
    private lateinit var resultNotice: TextView

    private var selectedCategory: ContentCategory? = null
    private var currentQuery: String = ""
    private val categoryChips = mutableListOf<Pair<TextView, ContentCategory?>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        libraryStore = LibraryStore(this)
        douyinAuthStore = DouyinAuthStore(this)
        buildScreen()
    }

    override fun onResume() {
        super.onResume()
        if (::recentAdapter.isInitialized) refreshContent()
    }

    private fun buildScreen() {
        val screenPadding = dp(48)
        val root = ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(getColorCompat(R.color.lark_background))
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(screenPadding, dp(34), screenPadding, dp(46))
        }
        root.addView(content, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ))

        content.addView(createBrandBlock())
        content.addView(createFeedEntry(), LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, dp(46)
        ).apply { topMargin = dp(16) })
        content.addView(createLibraryEntry(), LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, dp(42)
        ).apply { topMargin = dp(12) })
        content.addView(createDouyinLoginEntry(), LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, dp(42)
        ).apply { topMargin = dp(12) })
        content.addView(createSearchField(), LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(54)
        ).apply { topMargin = dp(22) })
        content.addView(createCategoryRow(), LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(52)
        ).apply { topMargin = dp(18) })

        content.addView(createSectionTitle(getString(R.string.featured)), sectionTitleParams())
        val featuredRow = createContentRow().also { row ->
            featuredAdapter = ContentCardAdapter(::openDetail).also {
                it.submitList(FeedProviderRegistry.active().loadInitial().take(6))
            }
            row.adapter = featuredAdapter
        }
        content.addView(featuredRow, rowParams())

        recentSection = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        recentSection.addView(createSectionTitle(getString(R.string.recently_watched)), sectionTitleParams())
        val recentRow = createContentRow().also { row ->
            recentAdapter = ContentCardAdapter(::openDetail)
            row.adapter = recentAdapter
        }
        recentSection.addView(recentRow, rowParams())
        content.addView(recentSection)

        contentHeading = createSectionTitle(getString(R.string.all_content))
        content.addView(contentHeading, sectionTitleParams())
        resultNotice = TextView(this).apply {
            setTextColor(getColorCompat(R.color.lark_text_secondary))
            textSize = 16f
            visibility = View.GONE
        }
        content.addView(resultNotice, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = dp(8) })
        val allContentRow = createContentRow().also { row ->
            contentAdapter = ContentCardAdapter(::openDetail)
            row.adapter = contentAdapter
        }
        content.addView(allContentRow, rowParams())

        content.addView(createNotice(), LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(18) })

        setContentView(root)
        refreshContent()
        featuredRow.post { featuredRow.getChildAt(0)?.requestFocus() }
    }

    private fun createBrandBlock(): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val title = TextView(this@MainActivity).apply {
                text = getString(R.string.app_name)
                setTextColor(getColorCompat(R.color.lark_accent))
                textSize = 34f
                setTypeface(typeface, Typeface.BOLD)
            }
            val subtitle = TextView(this@MainActivity).apply {
                text = getString(R.string.home_subtitle)
                setTextColor(getColorCompat(R.color.lark_text_secondary))
                textSize = 16f
                setPadding(0, dp(4), 0, 0)
            }
            addView(title)
            addView(subtitle)
        }
    }

    private fun createFeedEntry(): TextView = TextView(this).apply {
        text = "▶  开始沉浸刷流"
        textSize = 17f
        gravity = Gravity.CENTER
        isFocusable = true
        isFocusableInTouchMode = true
        setTextColor(getColorCompat(R.color.lark_background))
        setPadding(dp(20), 0, dp(20), 0)
        background = roundedDrawable(getColorCompat(R.color.lark_accent), dp(12))
        setOnClickListener {
            startActivity(Intent(this@MainActivity, ImmersiveFeedActivity::class.java))
        }
        onFocusChangeListener = View.OnFocusChangeListener { view, focused ->
            view.background = roundedDrawable(
                getColorCompat(R.color.lark_accent),
                dp(12),
                if (focused) getColorCompat(R.color.lark_focus) else Color.TRANSPARENT,
                if (focused) dp(2) else 0
            )
            view.animate().scaleX(if (focused) 1.04f else 1f).scaleY(if (focused) 1.04f else 1f)
                .setDuration(120L).start()
        }
    }

    private fun createLibraryEntry(): TextView = TextView(this).apply {
        text = "☆  我的收藏与历史"
        textSize = 16f
        gravity = Gravity.CENTER
        isFocusable = true
        isFocusableInTouchMode = true
        setTextColor(getColorCompat(R.color.lark_text_primary))
        setPadding(dp(18), 0, dp(18), 0)
        background = roundedDrawable(getColorCompat(R.color.lark_surface), dp(12))
        setOnClickListener {
            startActivity(Intent(this@MainActivity, LibraryActivity::class.java))
        }
        onFocusChangeListener = View.OnFocusChangeListener { view, focused ->
            view.background = roundedDrawable(
                getColorCompat(R.color.lark_surface_elevated),
                dp(12),
                if (focused) getColorCompat(R.color.lark_accent) else Color.TRANSPARENT,
                if (focused) dp(2) else 0
            )
        }
    }

    private fun createDouyinLoginEntry(): TextView = TextView(this).apply {
        text = if (douyinAuthStore.isLoggedIn) {
            getString(R.string.douyin_logged_in_short, douyinAuthStore.displayName ?: getString(R.string.douyin_user_default))
        } else {
            getString(R.string.douyin_login_entry)
        }
        textSize = 16f
        gravity = Gravity.CENTER
        isFocusable = true
        isFocusableInTouchMode = true
        setTextColor(getColorCompat(R.color.lark_text_primary))
        setPadding(dp(18), 0, dp(18), 0)
        background = roundedDrawable(getColorCompat(R.color.lark_surface), dp(12))
        setOnClickListener { startActivity(Intent(this@MainActivity, DouyinLoginActivity::class.java)) }
        onFocusChangeListener = View.OnFocusChangeListener { view, focused ->
            view.background = roundedDrawable(
                getColorCompat(R.color.lark_surface_elevated),
                dp(12),
                if (focused) getColorCompat(R.color.lark_accent) else Color.TRANSPARENT,
                if (focused) dp(2) else 0
            )
        }
    }

    private fun createSearchField(): View {
        return EditText(this).apply {
            hint = getString(R.string.search_hint)
            setHintTextColor(getColorCompat(R.color.lark_text_secondary))
            setTextColor(getColorCompat(R.color.lark_text_primary))
            textSize = 18f
            setSingleLine(true)
            inputType = InputType.TYPE_CLASS_TEXT
            setPadding(dp(18), 0, dp(18), 0)
            background = roundedDrawable(getColorCompat(R.color.lark_surface), dp(14))
            onFocusChangeListener = View.OnFocusChangeListener { view, focused ->
                view.background = roundedDrawable(
                    if (focused) getColorCompat(R.color.lark_surface_elevated) else getColorCompat(R.color.lark_surface),
                    dp(14),
                    if (focused) getColorCompat(R.color.lark_accent) else Color.TRANSPARENT,
                    if (focused) dp(2) else 0
                )
            }
            addTextChangedListener { editable ->
                currentQuery = editable?.toString().orEmpty()
                refreshContent()
            }
        }
    }

    private fun createCategoryRow(): View {
        val scroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
        }
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val entries = listOf<Pair<String, ContentCategory?>>(
            getString(R.string.category_all) to null,
            getString(R.string.category_discover) to ContentCategory.DISCOVER,
            getString(R.string.category_music) to ContentCategory.MUSIC,
            getString(R.string.category_lifestyle) to ContentCategory.LIFESTYLE,
            getString(R.string.category_knowledge) to ContentCategory.KNOWLEDGE
        )
        entries.forEach { (label, category) ->
            val chip = createCategoryChip(label, category)
            categoryChips += chip to category
            container.addView(chip, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(40)
            ).apply { marginEnd = dp(12) })
        }
        scroll.addView(container)
        return scroll
    }

    private fun createCategoryChip(label: String, category: ContentCategory?): TextView {
        return TextView(this).apply {
            text = label
            gravity = Gravity.CENTER
            textSize = 16f
            isFocusable = true
            isFocusableInTouchMode = true
            setTextColor(getColorCompat(R.color.lark_text_primary))
            setPadding(dp(18), 0, dp(18), 0)
            updateChipBackground(this, category, false)
            setOnClickListener {
                selectedCategory = category
                refreshCategoryChips()
                refreshContent()
            }
            onFocusChangeListener = View.OnFocusChangeListener { view, focused ->
                updateChipBackground(view as TextView, category, focused)
            }
        }
    }

    private fun updateChipBackground(chip: TextView, category: ContentCategory?, focused: Boolean) {
        val selected = category == selectedCategory
        chip.background = roundedDrawable(
            when {
                selected -> getColorCompat(R.color.lark_accent_dark)
                focused -> getColorCompat(R.color.lark_surface_elevated)
                else -> getColorCompat(R.color.lark_surface)
            },
            dp(20),
            if (focused) getColorCompat(R.color.lark_accent) else Color.TRANSPARENT,
            if (focused) dp(2) else 0
        )
    }

    private fun refreshCategoryChips() {
        categoryChips.forEach { (chip, category) ->
            updateChipBackground(chip, category, chip.hasFocus())
        }
    }

    private fun createContentRow(): RecyclerView {
        return RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity, RecyclerView.HORIZONTAL, false)
            overScrollMode = View.OVER_SCROLL_NEVER
            clipToPadding = false
            setPadding(0, 0, dp(24), 0)
        }
    }

    private fun createSectionTitle(title: String): TextView {
        return TextView(this).apply {
            text = title
            setTextColor(getColorCompat(R.color.lark_text_primary))
            textSize = 22f
            setTypeface(typeface, Typeface.BOLD)
        }
    }

    private fun createNotice(): TextView {
        return TextView(this).apply {
            text = getString(R.string.demo_notice)
            setTextColor(getColorCompat(R.color.lark_text_secondary))
            textSize = 14f
            setPadding(dp(16), dp(14), dp(16), dp(14))
            background = roundedDrawable(getColorCompat(R.color.lark_surface), dp(12))
        }
    }

    private fun sectionTitleParams() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply {
        topMargin = dp(30)
        bottomMargin = dp(2)
    }

    private fun rowParams() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        dp(236)
    )

    private fun refreshContent() {
        val provider = FeedProviderRegistry.active()
        val catalog = provider.loadInitial()
        val filtered = provider.search(currentQuery).filter { item ->
            selectedCategory == null || item.category == selectedCategory
        }
        contentAdapter.submitList(filtered)
        featuredAdapter.submitList(
            catalog.filter { selectedCategory == null || it.category == selectedCategory }.take(6)
        )
        val recent = libraryStore.recent(catalog)
        recentAdapter.submitList(recent)
        recentSection.visibility = if (recent.isEmpty()) View.GONE else View.VISIBLE

        contentHeading.text = if (currentQuery.isBlank() && selectedCategory == null) {
            getString(R.string.all_content)
        } else {
            "搜索与筛选结果"
        }
        resultNotice.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        resultNotice.text = getString(R.string.search_empty)
    }

    private fun openDetail(item: VideoItem) {
        startActivity(Intent(this, DetailActivity::class.java).putExtra(DetailActivity.EXTRA_VIDEO_ID, item.id))
    }
}
