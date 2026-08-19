package app.streamlark.tv.ui

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import app.streamlark.tv.R
import app.streamlark.tv.auth.DouyinAuthStore

/**
 * Login shell for the official Douyin OAuth flow.
 *
 * The actual client_key, redirect URI and server-side code exchange are
 * deployment configuration. This screen deliberately opens only the official
 * authorization host and never asks the user for a password or cookie.
 */
class DouyinLoginActivity : AppCompatActivity() {
    private lateinit var authStore: DouyinAuthStore
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authStore = DouyinAuthStore(this)
        buildScreen()
    }

    override fun onResume() {
        super.onResume()
        if (::status.isInitialized) refreshStatus()
    }

    private fun buildScreen() {
        val root = ScrollView(this).apply {
            setBackgroundColor(getColorCompat(R.color.lark_background))
            isFillViewport = true
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(32), dp(42), dp(32), dp(42))
        }
        root.addView(content, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ))

        content.addView(TextView(this).apply {
            text = getString(R.string.douyin_login_title)
            textSize = 30f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(getColorCompat(R.color.lark_accent))
            gravity = Gravity.CENTER
        }, widthParams())
        content.addView(TextView(this).apply {
            text = getString(R.string.douyin_login_subtitle)
            textSize = 16f
            setTextColor(getColorCompat(R.color.lark_text_secondary))
            gravity = Gravity.CENTER
        }, widthParams(top = 10))

        status = TextView(this).apply {
            textSize = 18f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(dp(20), dp(18), dp(20), dp(18))
            background = roundedDrawable(getColorCompat(R.color.lark_surface), dp(14))
        }
        content.addView(status, widthParams(top = 28))

        content.addView(createAction(getString(R.string.douyin_open_authorization)) {
            // This is the public authorization landing page. The deployed
            // build should append the app's registered OAuth parameters.
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(DOUYIN_AUTHORIZATION_URL)))
        }, widthParams(top = 22))

        content.addView(createAction(getString(R.string.douyin_logout)) {
            authStore.clear()
            refreshStatus()
        }, widthParams(top = 12, secondary = true))

        content.addView(TextView(this).apply {
            text = getString(R.string.douyin_login_security_note)
            textSize = 14f
            setLineSpacing(0f, 1.18f)
            setTextColor(getColorCompat(R.color.lark_text_secondary))
            setPadding(0, dp(28), 0, 0)
        }, widthParams())

        setContentView(root)
        refreshStatus()
    }

    private fun refreshStatus() {
        status.text = if (authStore.isLoggedIn) {
            getString(R.string.douyin_logged_in, authStore.displayName ?: getString(R.string.douyin_user_default))
        } else {
            getString(R.string.douyin_not_logged_in)
        }
    }

    private fun createAction(label: String, action: () -> Unit): TextView = TextView(this).apply {
        text = label
        textSize = 17f
        gravity = Gravity.CENTER
        isFocusable = true
        isFocusableInTouchMode = true
        setPadding(dp(18), 0, dp(18), 0)
        setTextColor(if (label == getString(R.string.douyin_open_authorization)) getColorCompat(R.color.lark_background) else getColorCompat(R.color.lark_text_primary))
        background = roundedDrawable(
            if (label == getString(R.string.douyin_open_authorization)) getColorCompat(R.color.lark_accent) else getColorCompat(R.color.lark_surface),
            dp(12)
        )
        setOnClickListener { action() }
        onFocusChangeListener = View.OnFocusChangeListener { view, focused ->
            view.background = roundedDrawable(
                if (label == getString(R.string.douyin_open_authorization)) getColorCompat(R.color.lark_accent) else getColorCompat(R.color.lark_surface_elevated),
                dp(12),
                if (focused) getColorCompat(R.color.lark_focus) else Color.TRANSPARENT,
                if (focused) dp(2) else 0
            )
        }
    }

    private fun widthParams(top: Int = 0): LinearLayout.LayoutParams = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply { if (top > 0) topMargin = dp(top) }

    private fun widthParams(top: Int, secondary: Boolean): LinearLayout.LayoutParams = widthParams(top)

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun getColorCompat(id: Int): Int = androidx.core.content.ContextCompat.getColor(this, id)

    private fun roundedDrawable(color: Int, radius: Int, strokeColor: Int = Color.TRANSPARENT, strokeWidth: Int = 0) =
        android.graphics.drawable.GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius.toFloat()
            if (strokeWidth > 0) setStroke(strokeWidth, strokeColor)
        }

    private companion object {
        const val DOUYIN_AUTHORIZATION_URL = "https://open.douyin.com/platform/oauth/connect/"
    }
}
