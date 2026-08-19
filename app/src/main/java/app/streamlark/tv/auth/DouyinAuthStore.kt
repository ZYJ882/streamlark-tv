package app.streamlark.tv.auth

import android.content.Context

/**
 * Local view of the Douyin authorization state.
 *
 * Access/refresh tokens must be exchanged and stored by the authorized backend;
 * this Android client intentionally keeps only display metadata and a session
 * marker. It never accepts browser cookies or private API credentials.
 */
class DouyinAuthStore(context: Context) {
    private val preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    val isLoggedIn: Boolean
        get() = preferences.getBoolean(KEY_LOGGED_IN, false)

    val displayName: String?
        get() = preferences.getString(KEY_DISPLAY_NAME, null)

    val avatarUrl: String?
        get() = preferences.getString(KEY_AVATAR_URL, null)

    fun markAuthorized(displayName: String?, avatarUrl: String? = null) {
        preferences.edit()
            .putBoolean(KEY_LOGGED_IN, true)
            .putString(KEY_DISPLAY_NAME, displayName?.takeIf { it.isNotBlank() })
            .putString(KEY_AVATAR_URL, avatarUrl?.takeIf { it.isNotBlank() })
            .apply()
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private companion object {
        const val FILE_NAME = "douyin_auth"
        const val KEY_LOGGED_IN = "logged_in"
        const val KEY_DISPLAY_NAME = "display_name"
        const val KEY_AVATAR_URL = "avatar_url"
    }
}
