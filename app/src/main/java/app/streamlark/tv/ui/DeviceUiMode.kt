package app.streamlark.tv.ui

import android.content.Context
import android.content.pm.PackageManager

enum class DeviceUiMode {
    MOBILE,
    TELEVISION
}

/**
 * Mirrors the capability-first approach used by VLC Android: shared playback
 * logic, but a different navigation shell for Leanback/no-touch devices.
 */
object DeviceUiRouter {
    private const val PREFERENCES = "streamlark_ui_mode"
    private const val KEY_OVERRIDE = "override"
    private const val FEATURE_LEANBACK = "android.software.leanback"
    private const val FEATURE_TOUCHSCREEN = "android.hardware.touchscreen"

    fun current(context: Context): DeviceUiMode {
        val override = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .getString(KEY_OVERRIDE, null)
        return resolve(
            hasLeanback = context.packageManager.hasSystemFeature(FEATURE_LEANBACK),
            hasTouchscreen = context.packageManager.hasSystemFeature(FEATURE_TOUCHSCREEN),
            override = override
        )
    }

    fun resolve(
        hasLeanback: Boolean,
        hasTouchscreen: Boolean,
        override: String? = null
    ): DeviceUiMode {
        return when (override) {
            DeviceUiMode.MOBILE.name -> DeviceUiMode.MOBILE
            DeviceUiMode.TELEVISION.name -> DeviceUiMode.TELEVISION
            else -> if (hasLeanback || !hasTouchscreen) DeviceUiMode.TELEVISION else DeviceUiMode.MOBILE
        }
    }

    fun setOverride(context: Context, mode: DeviceUiMode?) {
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit().apply {
            if (mode == null) remove(KEY_OVERRIDE) else putString(KEY_OVERRIDE, mode.name)
        }.apply()
    }
}
