package app.streamlark.tv.model

/**
 * Provider-neutral creator profile. It intentionally does not include login
 * cookies, contact information or third-party account credentials.
 */
data class CreatorProfile(
    val id: String,
    val displayName: String,
    val handle: String,
    val bio: String,
    val followerLabel: String,
    val accentColorHex: String
)

data class ContentCollection(
    val id: String,
    val ownerId: String,
    val title: String,
    val subtitle: String,
    val videoIds: List<String>,
    val accentColorHex: String
)
