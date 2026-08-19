package app.streamlark.tv.ui

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.streamlark.tv.R
import app.streamlark.tv.model.ContentCollection

class CollectionCardAdapter(
    private val onClick: (ContentCollection) -> Unit
) : RecyclerView.Adapter<CollectionCardAdapter.CollectionViewHolder>() {

    private var items: List<ContentCollection> = emptyList()

    fun submitList(nextItems: List<ContentCollection>) {
        items = nextItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionViewHolder {
        val context = parent.context
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            isFocusable = true
            isFocusableInTouchMode = true
            setPadding(context.dp(18), context.dp(16), context.dp(18), context.dp(16))
            layoutParams = RecyclerView.LayoutParams(context.dp(286), context.dp(148)).apply {
                marginEnd = context.dp(16)
                topMargin = context.dp(8)
                bottomMargin = context.dp(8)
            }
        }
        val badge = TextView(context).apply {
            id = R.id.collection_badge
            textSize = 13f
            setTextColor(Color.WHITE)
            setPadding(context.dp(10), context.dp(4), context.dp(10), context.dp(4))
            background = roundedDrawable(Color.argb(135, 16, 19, 27), context.dp(8))
        }
        val title = TextView(context).apply {
            id = R.id.collection_title
            textSize = 18f
            maxLines = 2
            setTextColor(context.getColorCompat(R.color.lark_text_primary))
            setPadding(0, context.dp(12), 0, context.dp(3))
        }
        val subtitle = TextView(context).apply {
            id = R.id.collection_subtitle
            textSize = 13f
            maxLines = 2
            setTextColor(context.getColorCompat(R.color.lark_text_secondary))
        }
        root.addView(badge)
        root.addView(title)
        root.addView(subtitle)
        return CollectionViewHolder(root)
    }

    override fun onBindViewHolder(holder: CollectionViewHolder, position: Int) {
        holder.bind(items[position], onClick)
    }

    override fun getItemCount(): Int = items.size

    class CollectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val root = itemView as LinearLayout
        private val badge = root.findViewById<TextView>(R.id.collection_badge)
        private val title = root.findViewById<TextView>(R.id.collection_title)
        private val subtitle = root.findViewById<TextView>(R.id.collection_subtitle)

        fun bind(item: ContentCollection, onClick: (ContentCollection) -> Unit) {
            root.background = roundedDrawable(Color.parseColor(item.accentColorHex), root.context.dp(14))
            badge.text = "合集 · ${item.videoIds.size} 条"
            title.text = item.title
            subtitle.text = item.subtitle
            root.contentDescription = "合集 ${item.title}，${item.videoIds.size} 条内容"
            root.setOnClickListener { onClick(item) }
            root.onFocusChangeListener = View.OnFocusChangeListener { _, focused ->
                root.background = roundedDrawable(
                    Color.parseColor(item.accentColorHex),
                    root.context.dp(14),
                    if (focused) root.context.getColorCompat(R.color.lark_focus) else Color.TRANSPARENT,
                    if (focused) root.context.dp(3) else 0
                )
                root.animate().scaleX(if (focused) 1.05f else 1f).scaleY(if (focused) 1.05f else 1f)
                    .setDuration(130L).start()
            }
        }
    }
}
