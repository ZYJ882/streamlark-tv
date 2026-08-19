package app.streamlark.tv.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import app.streamlark.tv.R
import app.streamlark.tv.model.VideoItem

class ContentCardAdapter(
    private val onClick: (VideoItem) -> Unit
) : RecyclerView.Adapter<ContentCardAdapter.CardViewHolder>() {

    private var items: List<VideoItem> = emptyList()

    fun submitList(nextItems: List<VideoItem>) {
        items = nextItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        return CardViewHolder(createCardView(parent.context))
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(items[position], onClick)
    }

    override fun getItemCount(): Int = items.size

    private fun createCardView(context: Context): View {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            isFocusable = true
            isFocusableInTouchMode = true
            setPadding(context.dp(4))
            layoutParams = RecyclerView.LayoutParams(context.dp(256), ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                marginEnd = context.dp(14)
                bottomMargin = context.dp(10)
                topMargin = context.dp(10)
            }
        }

        val artwork = FrameLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, context.dp(150))
        }
        val badge = TextView(context).apply {
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 12f
            setPadding(context.dp(10), context.dp(4), context.dp(10), context.dp(4))
            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.START).apply {
                topMargin = context.dp(12)
                marginStart = context.dp(12)
            }
            background = roundedDrawable(Color.argb(160, 16, 19, 27), context.dp(8))
        }
        val playGlyph = TextView(context).apply {
            text = "▶"
            textSize = 34f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = roundedDrawable(Color.argb(110, 0, 0, 0), context.dp(28))
            layoutParams = FrameLayout.LayoutParams(context.dp(56), context.dp(56), Gravity.CENTER)
        }
        artwork.addView(badge)
        artwork.addView(playGlyph)

        val title = TextView(context).apply {
            id = R.id.card_title
            setTextColor(context.getColorCompat(R.color.lark_text_primary))
            textSize = 16f
            maxLines = 2
            setLineSpacing(0f, 1.05f)
            setPadding(context.dp(12), context.dp(12), context.dp(12), context.dp(2))
        }
        val subtitle = TextView(context).apply {
            id = R.id.card_subtitle
            setTextColor(context.getColorCompat(R.color.lark_text_secondary))
            textSize = 13f
            maxLines = 1
            setPadding(context.dp(12), 0, context.dp(12), context.dp(12))
        }

        container.addView(artwork)
        container.addView(title)
        container.addView(subtitle)
        return container
    }

    class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val root = itemView as LinearLayout
        private val artwork = root.getChildAt(0) as FrameLayout
        private val badge = artwork.getChildAt(0) as TextView
        private val title = root.findViewById<TextView>(R.id.card_title)
        private val subtitle = root.findViewById<TextView>(R.id.card_subtitle)

        fun bind(item: VideoItem, onClick: (VideoItem) -> Unit) {
            artwork.background = roundedDrawable(Color.parseColor(item.accentColorHex), root.context.dp(14))
            badge.text = item.badge
            title.text = item.title
            subtitle.text = "${item.creator} · ${item.durationLabel}"
            root.contentDescription = "${item.title}，${item.creator}，${item.durationLabel}"
            root.setOnClickListener { onClick(item) }
            root.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                root.animate().scaleX(if (hasFocus) 1.06f else 1f).scaleY(if (hasFocus) 1.06f else 1f)
                    .setDuration(130L).start()
                root.background = roundedDrawable(
                    if (hasFocus) root.context.getColorCompat(R.color.lark_surface_elevated)
                    else Color.TRANSPARENT,
                    root.context.dp(16),
                    if (hasFocus) root.context.getColorCompat(R.color.lark_accent) else Color.TRANSPARENT,
                    if (hasFocus) root.context.dp(3) else 0
                )
            }
        }
    }
}

internal fun Context.dp(value: Int): Int =
    (value * resources.displayMetrics.density).toInt()

internal fun Context.getColorCompat(colorRes: Int): Int =
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) getColor(colorRes)
    else @Suppress("DEPRECATION") resources.getColor(colorRes)

internal fun roundedDrawable(
    color: Int,
    radius: Int,
    strokeColor: Int = Color.TRANSPARENT,
    strokeWidth: Int = 0
): GradientDrawable = GradientDrawable().apply {
    shape = GradientDrawable.RECTANGLE
    cornerRadius = radius.toFloat()
    setColor(color)
    if (strokeWidth > 0) setStroke(strokeWidth, strokeColor)
}
