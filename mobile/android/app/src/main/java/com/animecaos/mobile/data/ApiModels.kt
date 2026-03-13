package com.animecaos.mobile.data

data class HealthResponse(
    val status: String
)

data class SearchResponse(
    val query: String,
    val titles: List<String>
)

data class EpisodesResponse(
    val anime: String,
    val episodes: List<String>
)

data class PlayerUrlResponse(
    val anime: String,
    val episode_index: Int,
    val player_url: String
)

data class AnimeMetaResponse(
    val query: String,
    val description: String?,
    val cover_url: String?
)

data class WatchlistResponse(
    val animes: List<String>
)

data class ContinueWatchingApiItem(
    val anime: String,
    val episode_index: Int,
    val progress: Float
)

data class ContinueWatchingResponse(
    val items: List<ContinueWatchingApiItem>
)

data class HomeFeedItemResponse(
    val display_title: String,
    val canonical_title: String
)

data class HomeFeedResponse(
    val trending: List<HomeFeedItemResponse>,
    val recent: List<HomeFeedItemResponse>,
    val recommended: List<HomeFeedItemResponse>,
    val generated_at: String,
    val cache_ttl_seconds: Int
)
