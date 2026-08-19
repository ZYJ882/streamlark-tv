package app.streamlark.tv.data

import android.content.Context
import app.streamlark.tv.model.VideoItem

/**
 * Lightweight local-first library for Android 5.0+. It never stores account
 * cookies, authorization headers, or remote-provider credentials.
 */
class LibraryStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun isFavorite(videoId: String): Boolean = favorites().contains(videoId)

    fun toggleFavorite(videoId: String): Boolean {
        val current = favorites().toMutableSet()
        val isNowFavorite = if (current.contains(videoId)) {
            current.remove(videoId)
            false
        } else {
            current.add(videoId)
            true
        }
        preferences.edit().putStringSet(KEY_FAVORITES, current).apply()
        return isNowFavorite
    }

    fun progressFor(videoId: String): Long = preferences.getLong(progressKey(videoId), 0L)

    fun saveProgress(videoId: String, positionMs: Long) {
        preferences.edit()
            .putLong(progressKey(videoId), positionMs.coerceAtLeast(0L))
            .putLong(historyKey(videoId), System.currentTimeMillis())
            .apply()
    }

    fun recent(catalog: List<VideoItem>, limit: Int = 8): List<VideoItem> {
        return catalog
            .mapNotNull { item ->
                val timestamp = preferences.getLong(historyKey(item.id), 0L)
                if (timestamp > 0L) item to timestamp else null
            }
            .sortedByDescending { (_, timestamp) -> timestamp }
            .take(limit)
            .map { (item, _) -> item }
    }

    private fun favorites(): Set<String> =
        preferences.getStringSet(KEY_FAVORITES, emptySet())?.toSet().orEmpty()

    private fun progressKey(videoId: String) = "progress_$videoId"

    private fun historyKey(videoId: String) = "history_$videoId"

    private companion object {
        const val PREFERENCES_NAME = "streamlark_library"
        const val KEY_FAVORITES = "favorites"
    }
}
