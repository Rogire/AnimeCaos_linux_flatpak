package com.animecaos.mobile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animecaos.mobile.data.ApiClient
import com.animecaos.mobile.data.ContinueWatchingApiItem
import com.animecaos.mobile.data.PlayerUrlResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import retrofit2.HttpException
import kotlin.math.abs

data class ContinueWatchingItem(
    val anime: String,
    val episodeLabel: String,
    val progress: Float,
)

data class MainUiState(
    val backendStatus: String = "Verificando backend...",
    val isSearching: Boolean = false,
    val isLoadingEpisodes: Boolean = false,
    val isResolvingPlayer: Boolean = false,
    val errorMessage: String? = null,
    val searchTitles: List<String> = emptyList(),
    val episodesByAnime: Map<String, List<String>> = emptyMap(),
    val activePlayerUrl: String? = null,
    val animeCoversByTitle: Map<String, String> = emptyMap(),
    val animeDescriptionsByTitle: Map<String, String> = emptyMap(),
    val resolvedTitlesByDisplay: Map<String, String> = emptyMap(),
    val watchlist: List<String> = emptyList(),
    val continueWatching: List<ContinueWatchingItem> = emptyList(),
)

class MainViewModel : ViewModel() {
    private val _state = MutableStateFlow(MainUiState())
    val state: StateFlow<MainUiState> = _state.asStateFlow()
    private val resolvedMetaTitles = mutableSetOf<String>()
    private val inFlightMetaTitles = mutableSetOf<String>()
    private var libraryDataLoaded = false

    private fun httpErrorMessage(error: HttpException): String {
        val rawBody = runCatching { error.response()?.errorBody()?.string() }.getOrNull().orEmpty()
        if (rawBody.isNotBlank()) {
            val detail = runCatching { JSONObject(rawBody).optString("detail") }.getOrNull().orEmpty().trim()
            if (detail.isNotBlank()) return detail
        }
        return "Erro do backend (${error.code()})."
    }

    private fun networkErrorMessage(error: Throwable): String {
        return when (error) {
            is SocketTimeoutException, is InterruptedIOException -> "Timeout ao conectar. Verifique se o backend esta rodando (uvicorn --host 0.0.0.0 --port 8000)."
            is TimeoutCancellationException -> "Tempo limite excedido. Verifique se o backend esta ativo e acessivel."
            is ConnectException -> "Nao foi possivel conectar ao backend. Inicie o servidor e tente novamente."
            is UnknownHostException -> "Host do backend nao foi resolvido. Verifique a configuracao de rede."
            is HttpException -> httpErrorMessage(error)
            else -> error.message ?: "Falha na requisicao de rede."
        }
    }

    private fun persistResolvedTitle(displayTitle: String, canonicalTitle: String) {
        if (displayTitle.isBlank() || canonicalTitle.isBlank()) return
        _state.update { current ->
            val updated = current.resolvedTitlesByDisplay.toMutableMap()
            updated[displayTitle] = canonicalTitle
            current.copy(resolvedTitlesByDisplay = updated)
        }
    }

    private fun updateAnimeMeta(title: String, description: String?, coverUrl: String?) {
        if (title.isBlank()) return
        _state.update { current ->
            val covers = current.animeCoversByTitle.toMutableMap()
            val descriptions = current.animeDescriptionsByTitle.toMutableMap()
            if (!coverUrl.isNullOrBlank()) {
                covers[title] = coverUrl
            }
            if (!description.isNullOrBlank()) {
                descriptions[title] = description.trim()
            }
            current.copy(
                animeCoversByTitle = covers,
                animeDescriptionsByTitle = descriptions,
            )
        }
    }

    private fun normalizeTitleForMatch(value: String): String {
        return value
            .trim()
            .lowercase()
            .replace("(dublado)", "")
            .replace("(legendado)", "")
            .replace(Regex("[^a-z0-9]+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun candidateScore(query: String, candidate: String): Int {
        val normalizedQuery = normalizeTitleForMatch(query)
        if (normalizedQuery.isBlank()) return Int.MIN_VALUE
        val queryTokens = normalizedQuery.split(" ").filter { it.isNotBlank() }
        val normalizedCandidate = normalizeTitleForMatch(candidate)

        return when {
            normalizedCandidate == normalizedQuery -> 1000
            normalizedCandidate.startsWith(normalizedQuery) -> 920 - (normalizedCandidate.length - normalizedQuery.length)
            normalizedCandidate.contains(normalizedQuery) -> 860 - (normalizedCandidate.length - normalizedQuery.length)
            normalizedQuery.contains(normalizedCandidate) -> 760 - (normalizedQuery.length - normalizedCandidate.length)
            else -> {
                val candidateTokens = normalizedCandidate.split(" ").filter { it.isNotBlank() }
                val overlap = queryTokens.count { it in candidateTokens }
                overlap * 120 - abs(candidateTokens.size - queryTokens.size) * 12
            }
        }
    }

    private fun bestTitleMatch(query: String, candidates: List<String>): String? {
        if (candidates.isEmpty()) return null
        return candidates.maxByOrNull { candidateScore(query, it) }
    }

    private fun rankedCandidates(query: String, candidates: List<String>): List<String> {
        return candidates
            .distinct()
            .sortedByDescending { candidateScore(query, it) }
    }

    private fun mapContinueWatchingItems(items: List<ContinueWatchingApiItem>): List<ContinueWatchingItem> {
        return items
            .filter { it.anime.isNotBlank() && it.episode_index >= 0 }
            .map { item ->
                ContinueWatchingItem(
                    anime = item.anime.trim(),
                    episodeLabel = "Episodio ${item.episode_index + 1}",
                    progress = item.progress.coerceIn(0f, 1f),
                )
            }
    }

    private suspend fun fetchWatchlistFromApi(): List<String> {
        return withTimeout(20_000L) {
            ApiClient.api.watchlist().animes
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinct()
        }
    }

    private suspend fun fetchContinueWatchingFromApi(): List<ContinueWatchingItem> {
        return withTimeout(20_000L) {
            mapContinueWatchingItems(ApiClient.api.continueWatching().items)
        }
    }

    private fun refreshContinueWatching() {
        viewModelScope.launch {
            runCatching { fetchContinueWatchingFromApi() }
                .onSuccess { items ->
                    _state.update { current ->
                        current.copy(
                            continueWatching = items,
                            errorMessage = null,
                        )
                    }
                }
        }
    }

    fun loadLibraryData(forceRefresh: Boolean = false) {
        if (libraryDataLoaded && !forceRefresh) return

        viewModelScope.launch {
            val watchlistResult = runCatching { fetchWatchlistFromApi() }
            val continueResult = runCatching { fetchContinueWatchingFromApi() }

            _state.update { current ->
                val firstError = if (watchlistResult.isFailure && continueResult.isFailure) {
                    watchlistResult.exceptionOrNull() ?: continueResult.exceptionOrNull()
                } else {
                    null
                }
                current.copy(
                    watchlist = watchlistResult.getOrElse { current.watchlist },
                    continueWatching = continueResult.getOrElse { current.continueWatching },
                    errorMessage = firstError?.let { networkErrorMessage(it) } ?: current.errorMessage,
                )
            }
            if (watchlistResult.isSuccess || continueResult.isSuccess) {
                libraryDataLoaded = true
            }
        }
    }

    fun ensureAnimeMeta(title: String) {
        val normalized = title.trim()
        if (normalized.isBlank()) return
        if (resolvedMetaTitles.contains(normalized) || inFlightMetaTitles.contains(normalized)) return
        inFlightMetaTitles.add(normalized)

        viewModelScope.launch {
            var loaded = false

            runCatching { ApiClient.api.animeMeta(normalized) }
                .onSuccess { response ->
                    val fullCover = ApiClient.absoluteUrl(response.cover_url)
                    updateAnimeMeta(normalized, response.description, fullCover)
                    resolvedMetaTitles.add(normalized)
                    loaded = true
                }

            if (!loaded) {
                delay(350)
                runCatching { ApiClient.api.animeMeta(normalized) }
                    .onSuccess { response ->
                        val fullCover = ApiClient.absoluteUrl(response.cover_url)
                        updateAnimeMeta(normalized, response.description, fullCover)
                        resolvedMetaTitles.add(normalized)
                        loaded = true
                    }
            }

            inFlightMetaTitles.remove(normalized)
        }
    }

    fun prefetchAnimeMeta(titles: List<String>) {
        titles.distinct().take(24).forEach { ensureAnimeMeta(it) }
    }

    private suspend fun resolveCanonicalTitle(displayTitle: String): String {
        val normalized = displayTitle.trim()
        if (normalized.isBlank()) return normalized

        val fromState = _state.value.resolvedTitlesByDisplay[normalized]
        if (!fromState.isNullOrBlank()) return fromState

        val exactInCurrentSearch = _state.value.searchTitles.firstOrNull { it.equals(normalized, ignoreCase = true) }
        if (!exactInCurrentSearch.isNullOrBlank()) {
            persistResolvedTitle(normalized, exactInCurrentSearch)
            ensureAnimeMeta(exactInCurrentSearch)
            return exactInCurrentSearch
        }

        if (normalized.length < 2) return normalized

        val candidates = runCatching {
            withTimeout(20_000L) { ApiClient.api.search(normalized).titles }
        }.getOrDefault(emptyList())

        val candidate = bestTitleMatch(normalized, candidates).orEmpty()

        val canonical = if (candidate.isBlank()) normalized else candidate
        persistResolvedTitle(normalized, canonical)
        ensureAnimeMeta(canonical)
        return canonical
    }

    fun checkHealth() {
        viewModelScope.launch {
            runCatching { ApiClient.api.health() }
                .onSuccess { response ->
                    _state.update { current ->
                        current.copy(backendStatus = response.status, errorMessage = null)
                    }
                }
                .onFailure { error ->
                    _state.update { current ->
                        current.copy(
                            backendStatus = "offline",
                            errorMessage = networkErrorMessage(error),
                        )
                    }
                }
        }
    }

    fun search(query: String) {
        if (query.isBlank()) return

        _state.update { current ->
            current.copy(
                isSearching = true,
                errorMessage = null,
            )
        }
        viewModelScope.launch {
            val trimmedQuery = query.trim()
            var succeeded = false

            // First attempt
            runCatching { withTimeout(30_000L) { ApiClient.api.search(trimmedQuery) } }
                .onSuccess { response ->
                    prefetchAnimeMeta(response.titles)
                    _state.update { current ->
                        current.copy(
                            isSearching = false,
                            searchTitles = response.titles,
                            errorMessage = null,
                        )
                    }
                    succeeded = true
                }

            // Automatic retry on failure after a short delay
            if (!succeeded) {
                delay(1500)
                runCatching { withTimeout(30_000L) { ApiClient.api.search(trimmedQuery) } }
                    .onSuccess { response ->
                        prefetchAnimeMeta(response.titles)
                        _state.update { current ->
                            current.copy(
                                isSearching = false,
                                searchTitles = response.titles,
                                errorMessage = null,
                            )
                        }
                        succeeded = true
                    }
                    .onFailure { error ->
                        _state.update { current ->
                            current.copy(
                                isSearching = false,
                                errorMessage = networkErrorMessage(error),
                            )
                        }
                        // Re-check backend health so the status bar updates
                        checkHealth()
                    }
            }
        }
    }

    fun loadEpisodes(anime: String) {
        if (anime.isBlank()) return
        val existing = _state.value.episodesByAnime[anime]
        if (!existing.isNullOrEmpty()) return

        ensureAnimeMeta(anime)
        _state.update { current ->
            current.copy(
                isLoadingEpisodes = true,
                errorMessage = null,
            )
        }
        viewModelScope.launch {
            runCatching {
                var resolvedTitle = anime
                val episodes = withTimeout(45_000L) {
                    ApiClient.api.episodes(anime).episodes
                }
                var finalEpisodes = episodes

                if (finalEpisodes.isEmpty()) {
                    val candidates = runCatching {
                        withTimeout(20_000L) { ApiClient.api.search(anime).titles }
                    }.getOrDefault(emptyList())

                    for (candidate in rankedCandidates(anime, candidates).take(6)) {
                        if (candidate.equals(anime, ignoreCase = true)) continue
                        val candidateEpisodes = runCatching {
                            withTimeout(18_000L) { ApiClient.api.episodes(candidate).episodes }
                        }.getOrDefault(emptyList())
                        if (candidateEpisodes.isNotEmpty()) {
                            resolvedTitle = candidate
                            finalEpisodes = candidateEpisodes
                            persistResolvedTitle(anime, candidate)
                            ensureAnimeMeta(candidate)
                            break
                        }
                    }
                }

                resolvedTitle to finalEpisodes
            }
                .onSuccess { (canonical, episodes) ->
                    _state.update { current ->
                        val updated = current.episodesByAnime.toMutableMap()
                        updated[anime] = episodes
                        if (canonical.isNotBlank()) {
                            updated[canonical] = episodes
                        }
                        current.copy(
                            isLoadingEpisodes = false,
                            episodesByAnime = updated,
                            errorMessage = if (episodes.isEmpty()) "Nenhum episodio encontrado para este titulo." else null,
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { current ->
                        current.copy(
                            isLoadingEpisodes = false,
                            errorMessage = networkErrorMessage(error),
                        )
                    }
                }
        }
    }

    fun resolvePlayerUrl(anime: String, episodeIndex: Int) {
        if (anime.isBlank() || episodeIndex < 0) return
        _state.update { current ->
            current.copy(
                isResolvingPlayer = true,
                activePlayerUrl = null,
                errorMessage = null,
            )
        }
        viewModelScope.launch {
            runCatching { resolvePlayerUrlWithFallback(anime, episodeIndex) }
                .onSuccess { response ->
                    _state.update { current ->
                        current.copy(
                            isResolvingPlayer = false,
                            activePlayerUrl = response.player_url,
                            errorMessage = null,
                        )
                    }
                    refreshContinueWatching()
                }
                .onFailure { error ->
                    _state.update { current ->
                        current.copy(
                            isResolvingPlayer = false,
                            activePlayerUrl = null,
                            errorMessage = networkErrorMessage(error),
                        )
                    }
                }
        }
    }

    private suspend fun requestPlayerUrl(anime: String, episodeIndex: Int): PlayerUrlResponse {
        return withTimeout(45_000L) { ApiClient.api.playerUrl(anime, episodeIndex) }
    }

    private suspend fun resolvePlayerUrlWithFallback(anime: String, episodeIndex: Int): PlayerUrlResponse {
        val attemptedTitles = linkedSetOf<String>()
        var lastError: Throwable? = null

        suspend fun tryCandidate(rawTitle: String): PlayerUrlResponse? {
            val candidate = rawTitle.trim()
            if (candidate.isBlank()) return null

            val candidateKey = normalizeTitleForMatch(candidate)
            if (candidateKey.isBlank() || !attemptedTitles.add(candidateKey)) return null

            return runCatching { requestPlayerUrl(candidate, episodeIndex) }
                .onSuccess {
                    persistResolvedTitle(anime, candidate)
                    ensureAnimeMeta(candidate)
                }
                .onFailure { error -> lastError = error }
                .getOrNull()
        }

        tryCandidate(anime)?.let { return it }

        val mapped = _state.value.resolvedTitlesByDisplay[anime].orEmpty()
        if (mapped.isNotBlank() && !mapped.equals(anime, ignoreCase = true)) {
            tryCandidate(mapped)?.let { return it }
        }

        val canonical = runCatching { resolveCanonicalTitle(anime) }.getOrDefault("")
        if (canonical.isNotBlank() && !canonical.equals(anime, ignoreCase = true)) {
            tryCandidate(canonical)?.let { return it }
        }

        val searchCandidates = runCatching {
            withTimeout(20_000L) { ApiClient.api.search(anime).titles }
        }.getOrDefault(emptyList())

        for (candidate in rankedCandidates(anime, searchCandidates).take(8)) {
            tryCandidate(candidate)?.let { return it }
        }

        throw lastError ?: IllegalStateException("Nao foi possivel resolver uma fonte de player para este anime.")
    }

    fun clearActivePlayerUrl() {
        _state.update { current -> current.copy(activePlayerUrl = null) }
    }

    fun toggleWatchlist(anime: String) {
        val normalized = anime.trim()
        if (normalized.isBlank()) return

        viewModelScope.launch {
            val remove = _state.value.watchlist.any { it.equals(normalized, ignoreCase = true) }
            runCatching {
                withTimeout(20_000L) {
                    if (remove) {
                        ApiClient.api.removeWatchlist(normalized)
                    } else {
                        ApiClient.api.addWatchlist(normalized)
                    }
                }
            }
                .onSuccess { response ->
                    _state.update { current ->
                        current.copy(
                            watchlist = response.animes
                                .map { it.trim() }
                                .filter { it.isNotBlank() }
                                .distinct(),
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { current ->
                        current.copy(errorMessage = networkErrorMessage(error))
                    }
                }
        }
    }

    fun registerContinueWatching(anime: String, episodeIndex: Int) {
        if (anime.isBlank() || episodeIndex < 0) return
        refreshContinueWatching()
    }
}
