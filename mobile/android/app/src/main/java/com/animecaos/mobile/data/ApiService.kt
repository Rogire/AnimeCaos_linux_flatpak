package com.animecaos.mobile.data

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @GET("health")
    suspend fun health(): HealthResponse

    @GET("home-feed")
    suspend fun homeFeed(
        @Query("refresh") refresh: Boolean = false
    ): HomeFeedResponse

    @GET("anime-meta")
    suspend fun animeMeta(@Query("q") query: String): AnimeMetaResponse

    @GET("search")
    suspend fun search(@Query("q") query: String): SearchResponse

    @GET("episodes")
    suspend fun episodes(@Query("anime") anime: String): EpisodesResponse

    @GET("player-url")
    suspend fun playerUrl(
        @Query("anime") anime: String,
        @Query("episode_index") episodeIndex: Int
    ): PlayerUrlResponse

    @GET("watchlist")
    suspend fun watchlist(): WatchlistResponse

    @POST("watchlist/add")
    suspend fun addWatchlist(@Query("anime") anime: String): WatchlistResponse

    @POST("watchlist/remove")
    suspend fun removeWatchlist(@Query("anime") anime: String): WatchlistResponse

    @GET("continue-watching")
    suspend fun continueWatching(): ContinueWatchingResponse
}
