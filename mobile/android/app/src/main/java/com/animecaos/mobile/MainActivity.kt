package com.animecaos.mobile

import android.app.PictureInPictureParams
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.animecaos.mobile.ui.ContinueWatchingItem
import com.animecaos.mobile.ui.MainUiState
import com.animecaos.mobile.ui.MainViewModel
import com.animecaos.mobile.ui.player.PlayerMode
import com.animecaos.mobile.ui.player.PremiumPlayerLayer
import com.animecaos.mobile.ui.theme.AnimeCaosTheme
import com.animecaos.mobile.ui.theme.AnimeCaosTokens
import kotlinx.coroutines.delay
import kotlin.math.abs

private enum class MainTab(val label: String, val icon: ImageVector) {
    Home("Inicio", Icons.Outlined.Home),
    Library("Biblioteca", Icons.Outlined.VideoLibrary),
}

private val animeDescriptions = mapOf(
    "Jujutsu Kaisen" to "Fantasia sombria com exorcistas e batalhas intensas.",
    "Kaiju No. 8" to "Acao militar contra kaijus com ritmo acelerado.",
    "Blue Lock" to "Disputa intensa de atacantes com foco tatico.",
    "Frieren" to "Jornada emocional de fantasia apos a grande vitoria.",
    "Solo Leveling" to "Acao hunter com evolucao rapida e grandes riscos.",
    "Dan Da Dan" to "Caos paranormal com velocidade e humor absurdo.",
    "Sakamoto Days" to "Legado de assassino com combate estiloso.",
    "Chainsaw Man" to "Mundo de demonios com violencia e estilo cinematografico.",
    "Vinland Saga" to "Drama historico com vinganca e evolucao pessoal.",
    "Mushoku Tensei" to "Isekai de fantasia com construcao de mundo profunda.",
    "Demon Slayer" to "Batalhas de espada com visual refinado.",
    "Spy x Family" to "Comedia e acao com dinamica de familia improvavel.",
    "One Piece" to "Aventura pirata grandiosa com mundo expansivo.",
)

private fun artworkFor(anime: String, covers: Map<String, String>): String? = covers[anime]

private fun descriptionFor(anime: String, descriptions: Map<String, String>): String =
    descriptions[anime]
        ?: animeDescriptions[anime]
        ?: "Curadoria premium com navegacao fluida e foco cinematografico."

private fun posterBrushFor(title: String): Brush {
    val palettes = listOf(
        listOf(Color(0xFF1B2735), Color(0xFF090A0F)),
        listOf(Color(0xFF2A1F3D), Color(0xFF0B0A16)),
        listOf(Color(0xFF3D2A1F), Color(0xFF120C08)),
        listOf(Color(0xFF1F3D34), Color(0xFF07120F)),
        listOf(Color(0xFF3A2030), Color(0xFF10060C)),
    )
    val palette = palettes[abs(title.hashCode()) % palettes.size]
    return Brush.verticalGradient(palette)
}

class MainActivity : ComponentActivity() {
    private var canEnterPip by mutableStateOf(false)

    private fun tryEnterPipMode() {
        if (!canEnterPip) return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        if (isInPictureInPictureMode) return

        val params = PictureInPictureParams.Builder()
            .setAspectRatio(Rational(16, 9))
            .build()
        enterPictureInPictureMode(params)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            AnimeCaosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent,
                    contentColor = AnimeCaosTokens.TextPrimary,
                ) {
                    AnimeCaosApp(
                        onPipAvailabilityChanged = { allowed -> canEnterPip = allowed },
                    )
                }
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        tryEnterPipMode()
    }
}

@Composable
private fun AnimeCaosApp(
    viewModel: MainViewModel = viewModel(),
    onPipAvailabilityChanged: (Boolean) -> Unit = {},
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    var showSplash by rememberSaveable { mutableStateOf(true) }
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.Home) }
    var selectedAnime by rememberSaveable { mutableStateOf<String?>(null) }
    var playerAnime by rememberSaveable { mutableStateOf<String?>(null) }
    var playerEpisodeIndex by rememberSaveable { mutableIntStateOf(-1) }
    var playerMode by rememberSaveable { mutableStateOf(PlayerMode.Fullscreen) }

    LaunchedEffect(Unit) {
        viewModel.checkHealth()
        delay(1050)
        showSplash = false
    }
    LaunchedEffect(selectedTab) {
        if (selectedTab == MainTab.Library) {
            viewModel.loadLibraryData()
        }
    }
    LaunchedEffect(playerAnime, playerEpisodeIndex) {
        if (playerAnime != null && playerEpisodeIndex >= 0) {
            viewModel.resolvePlayerUrl(playerAnime.orEmpty(), playerEpisodeIndex)
            viewModel.registerContinueWatching(playerAnime.orEmpty(), playerEpisodeIndex)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AnimeCaosTokens.AppBackgroundBrush),
    ) {
        if (showSplash) {
            SplashScreen()
            return@Box
        }

        val hasActivePlayer = playerAnime != null && playerEpisodeIndex >= 0
        val allowPip = hasActivePlayer && !uiState.activePlayerUrl.isNullOrBlank()
        LaunchedEffect(allowPip) {
            onPipAvailabilityChanged(allowPip)
        }
        val showMainContent = !hasActivePlayer || playerMode == PlayerMode.Mini
        val closePlayer = {
            playerAnime = null
            playerEpisodeIndex = -1
            playerMode = PlayerMode.Fullscreen
            viewModel.clearActivePlayerUrl()
        }

        BackHandler(enabled = hasActivePlayer) {
            if (playerMode == PlayerMode.Fullscreen) {
                closePlayer()
            } else {
                playerMode = PlayerMode.Fullscreen
            }
        }
        BackHandler(enabled = !hasActivePlayer && selectedAnime != null) {
            selectedAnime = null
        }

        if (showMainContent) {
            if (selectedAnime != null) {
                AnimeDetailsScreen(
                    anime = selectedAnime.orEmpty(),
                    episodeTitles = uiState.episodesByAnime[selectedAnime].orEmpty(),
                    isLoadingEpisodes = uiState.isLoadingEpisodes,
                    watchlisted = uiState.watchlist.contains(selectedAnime),
                    animeDescription = descriptionFor(selectedAnime.orEmpty(), uiState.animeDescriptionsByTitle),
                    animeImageUrl = artworkFor(selectedAnime.orEmpty(), uiState.animeCoversByTitle),
                    onBack = { selectedAnime = null },
                    onLoadEpisodes = viewModel::loadEpisodes,
                    onEnsureAnimeMeta = viewModel::ensureAnimeMeta,
                    onToggleWatchlist = viewModel::toggleWatchlist,
                    onOpenEpisode = { anime, idx ->
                        val canonicalAnime = uiState.resolvedTitlesByDisplay[anime].orEmpty().ifBlank { anime }
                        playerAnime = canonicalAnime
                        playerEpisodeIndex = idx
                        playerMode = PlayerMode.Fullscreen
                    },
                )
            } else {
                MainShell(
                    state = uiState,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    onSearch = viewModel::search,
                    onOpenAnime = { selectedAnime = it },
                    onPrefetchAnimeMeta = viewModel::prefetchAnimeMeta,
                )
            }
        }

        if (hasActivePlayer) {
            PremiumPlayerLayer(
                mode = playerMode,
                anime = playerAnime.orEmpty(),
                episodeIndex = playerEpisodeIndex,
                episodeCount = uiState.episodesByAnime[playerAnime].orEmpty().size,
                playerUrl = uiState.activePlayerUrl,
                isResolving = uiState.isResolvingPlayer,
                errorMessage = uiState.errorMessage,
                onPrev = { if (playerEpisodeIndex > 0) playerEpisodeIndex -= 1 },
                onNext = {
                    val maxIndex = uiState.episodesByAnime[playerAnime].orEmpty().lastIndex
                    if (playerEpisodeIndex < maxIndex) playerEpisodeIndex += 1
                },
                onBack = {
                    closePlayer()
                },
                onMinimize = { playerMode = PlayerMode.Mini },
                onExpand = { playerMode = PlayerMode.Fullscreen },
                onClose = { closePlayer() },
            )
        }
    }
}

@Composable
private fun SplashScreen() {
    val transition = rememberInfiniteTransition(label = "splash")
    val progress by transition.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "splash_progress",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF060A14), Color(0xFF0D1423)),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(230.dp)
                .background(
                    Brush.radialGradient(
                        listOf(AnimeCaosTokens.SecondaryRedGlow.copy(alpha = 0.8f), Color.Transparent),
                    ),
                    shape = CircleShape,
                ),
        )
        GlassPanel(
            modifier = Modifier.width(258.dp),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.animecaos_logo),
                    contentDescription = "AnimeCaos logo",
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop,
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = AnimeCaosTokens.TextPrimary)) { append("Anime") }
                        withStyle(SpanStyle(color = AnimeCaosTokens.SecondaryRedSoft)) { append("Caos") }
                    },
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
                Text(
                "Streaming premium, navegacao fluida e experiencia cinematica.",
                color = AnimeCaosTokens.TextMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.1f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(AnimeCaosTokens.SecondaryRedSoft),
                )
            }
        }
    }
}

@Composable
private fun MainShell(
    state: MainUiState,
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    onSearch: (String) -> Unit,
    onOpenAnime: (String) -> Unit,
    onPrefetchAnimeMeta: (List<String>) -> Unit,
) {
    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            NavigationBar(
                containerColor = AnimeCaosTokens.SurfaceGlassStrong,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .border(
                        BorderStroke(1.dp, AnimeCaosTokens.BorderGlass),
                        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    )
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
            ) {
                MainTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { onTabSelected(tab) },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { padding ->
        Crossfade(
            targetState = selectedTab,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            modifier = Modifier.padding(padding),
        ) { tab ->
            when (tab) {
                MainTab.Home -> HomeScreen(
                    isSearching = state.isSearching,
                    errorMessage = state.errorMessage,
                    titles = state.searchTitles,
                    onSearch = onSearch,
                    onOpenAnime = onOpenAnime,
                    coverByAnime = state.animeCoversByTitle,
                    onPrefetchAnimeMeta = onPrefetchAnimeMeta,
                )
                MainTab.Library -> LibraryScreen(
                    watchlist = state.watchlist,
                    continueWatching = state.continueWatching,
                    onOpenAnime = onOpenAnime,
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(
    isSearching: Boolean,
    errorMessage: String?,
    titles: List<String>,
    onSearch: (String) -> Unit,
    onOpenAnime: (String) -> Unit,
    coverByAnime: Map<String, String>,
    onPrefetchAnimeMeta: (List<String>) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(titles) {
        if (titles.isNotEmpty()) onPrefetchAnimeMeta(titles)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GlassPanel(contentPadding = PaddingValues(0.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF26111A), Color(0xFF10141F)),
                        ),
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.animecaos_logo),
                        contentDescription = "AnimeCaos logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(11.dp)),
                    )
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = AnimeCaosTokens.TextPrimary)) { append("Anime") }
                            withStyle(SpanStyle(color = AnimeCaosTokens.SecondaryRedSoft)) { append("Caos") }
                        },
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
        }

        GlassPanel(contentPadding = PaddingValues(14.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    label = { Text("Nome do anime") },
                    shape = RoundedCornerShape(18.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { onSearch(query.trim()) },
                    ),
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = AnimeCaosTokens.SurfaceGlassStrong,
                        unfocusedContainerColor = AnimeCaosTokens.SurfaceGlass,
                        focusedIndicatorColor = AnimeCaosTokens.BorderFocus,
                        unfocusedIndicatorColor = AnimeCaosTokens.BorderGlass,
                        focusedTextColor = AnimeCaosTokens.TextPrimary,
                        unfocusedTextColor = AnimeCaosTokens.TextPrimary,
                        focusedLabelColor = AnimeCaosTokens.TextMuted,
                        unfocusedLabelColor = AnimeCaosTokens.TextMuted,
                    ),
                )
                Button(
                    onClick = { onSearch(query.trim()) },
                    enabled = query.isNotBlank(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AnimeCaosTokens.SecondaryRed,
                        contentColor = AnimeCaosTokens.TextOnRed,
                    ),
                ) {
                    Icon(Icons.Outlined.Search, contentDescription = "Buscar")
                }
            }
        }

        if (isSearching) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = AnimeCaosTokens.SecondaryRedSoft)
                Text("Buscando nas fontes...", color = AnimeCaosTokens.TextMuted, style = MaterialTheme.typography.bodyMedium)
            }
        }
        if (!errorMessage.isNullOrBlank()) {
            GlassPanel(contentPadding = PaddingValues(12.dp)) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Resultados", style = MaterialTheme.typography.titleMedium, color = AnimeCaosTokens.TextPrimary)
            Spacer(Modifier.weight(1f))
            Text("${titles.size}", color = AnimeCaosTokens.TextMuted, style = MaterialTheme.typography.labelLarge)
        }

        if (titles.isEmpty()) {
            GlassPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(0.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    AnimeCaosTokens.SecondaryRedGlow.copy(alpha = 0.2f),
                                    Color.Transparent,
                                ),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.animecaos_logo),
                        contentDescription = "AnimeCaos",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(20.dp)),
                        alpha = 0.35f,
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 128.dp),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 120.dp),
            ) {
                items(titles, key = { it }) { title ->
                    PosterCard(
                        title = title,
                        imageUrl = artworkFor(title, coverByAnime),
                        onClick = { onOpenAnime(title) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimeDetailsScreen(
    anime: String,
    episodeTitles: List<String>,
    isLoadingEpisodes: Boolean,
    watchlisted: Boolean,
    animeDescription: String,
    animeImageUrl: String?,
    onBack: () -> Unit,
    onLoadEpisodes: (String) -> Unit,
    onEnsureAnimeMeta: (String) -> Unit,
    onToggleWatchlist: (String) -> Unit,
    onOpenEpisode: (String, Int) -> Unit,
) {
    var descriptionExpanded by rememberSaveable(anime) { mutableStateOf(false) }
    val shouldShowDescriptionToggle =
        animeDescription.length > 220 || animeDescription.count { it == '\n' } >= 4

    LaunchedEffect(anime) {
        onEnsureAnimeMeta(anime)
        onLoadEpisodes(anime)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onBack,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AnimeCaosTokens.SurfaceGlassStrong,
                    contentColor = AnimeCaosTokens.TextPrimary,
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar")
                Spacer(Modifier.width(4.dp))
                Text("Voltar")
            }
            Text("Detalhes do anime", color = AnimeCaosTokens.TextMuted)
        }

        GlassPosterHeader(
            title = anime,
            imageUrl = animeImageUrl,
        )

        GlassPanel(contentPadding = PaddingValues(14.dp)) {
            Text(anime, style = MaterialTheme.typography.titleLarge)
            Text(
                text = animeDescription,
                color = AnimeCaosTokens.TextMuted,
                maxLines = if (descriptionExpanded) Int.MAX_VALUE else 4,
                overflow = TextOverflow.Ellipsis,
            )
            if (shouldShowDescriptionToggle) {
                TextButton(onClick = { descriptionExpanded = !descriptionExpanded }) {
                    Text(if (descriptionExpanded) "Recolher" else "Expandir")
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onToggleWatchlist(anime) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (watchlisted) AnimeCaosTokens.SecondaryRed else AnimeCaosTokens.SurfaceGlassStrong,
                        contentColor = AnimeCaosTokens.TextPrimary,
                    ),
                ) { Text(if (watchlisted) "Na lista" else "Minha lista") }
                Button(
                    onClick = { onOpenEpisode(anime, 0) },
                    enabled = episodeTitles.isNotEmpty(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AnimeCaosTokens.SecondaryRed,
                        contentColor = AnimeCaosTokens.TextOnRed,
                    ),
                ) {
                    Icon(Icons.Outlined.PlayArrow, contentDescription = "Assistir")
                    Spacer(Modifier.width(6.dp))
                    Text("Assistir")
                }
            }
        }

        GlassPanel(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp)) {
            Text("Episodios", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))

            if (isLoadingEpisodes) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = AnimeCaosTokens.SecondaryRedSoft)
                    Text("Carregando episodios...", color = AnimeCaosTokens.TextMuted)
                }
            } else if (episodeTitles.isEmpty()) {
                Text("Nenhum episodio disponivel.", color = AnimeCaosTokens.TextMuted)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 10.dp)) {
                    itemsIndexed(episodeTitles) { index, title ->
                        EpisodeRow(index = index, title = title, onPlay = { onOpenEpisode(anime, index) })
                    }
                }
            }
        }
    }
}

@Composable
private fun EpisodePlayerScreen(
    anime: String,
    episodeIndex: Int,
    episodeCount: Int,
    playerUrl: String?,
    isResolving: Boolean,
    onBack: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onResolve: (String, Int) -> Unit,
) {
    LaunchedEffect(anime, episodeIndex) { onResolve(anime, episodeIndex) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar")
                Spacer(Modifier.width(4.dp))
                Text("Detalhes")
            }
            Text("${episodeIndex + 1} / ${if (episodeCount <= 0) "-" else episodeCount}", color = AnimeCaosTokens.TextMuted)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF030406))
                .border(BorderStroke(1.dp, AnimeCaosTokens.BorderGlass), RoundedCornerShape(20.dp)),
        ) {
            Text("Modo foco de video", modifier = Modifier.align(Alignment.Center), color = AnimeCaosTokens.TextMuted)
        }

        GlassPanel(contentPadding = PaddingValues(14.dp)) {
            Text(anime, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleMedium)
            Text("Episodio ${episodeIndex + 1}", color = AnimeCaosTokens.TextMuted)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onPrev,
                    enabled = episodeIndex > 0,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AnimeCaosTokens.SurfaceGlassStrong,
                        contentColor = AnimeCaosTokens.TextPrimary,
                    ),
                ) { Text("Anterior") }
                Button(
                    onClick = onNext,
                    enabled = episodeIndex + 1 < episodeCount,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AnimeCaosTokens.SurfaceGlassStrong,
                        contentColor = AnimeCaosTokens.TextPrimary,
                    ),
                ) { Text("Proximo") }
            }
            Spacer(Modifier.height(8.dp))
            if (isResolving) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = AnimeCaosTokens.SecondaryRedSoft)
                    Text("Resolvendo stream...", color = AnimeCaosTokens.TextMuted)
                }
            } else {
                Text(
                    text = if (playerUrl.isNullOrBlank()) "URL de transmissao nao resolvida." else "Transmissao: $playerUrl",
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun LibraryScreen(
    watchlist: List<String>,
    continueWatching: List<ContinueWatchingItem>,
    onOpenAnime: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Biblioteca", style = MaterialTheme.typography.headlineMedium, color = AnimeCaosTokens.TextPrimary)
                Text("Sua organizacao pessoal e progresso recente.", color = AnimeCaosTokens.TextMuted)
            }
        }
        item {
            LibrarySectionCard(
                title = "Minha lista",
                emptyMessage = "Nenhum titulo salvo.",
                entries = watchlist.map { it to "Salvo" },
                onOpenAnime = onOpenAnime,
            )
        }
        item {
            LibrarySectionCard(
                title = "Continuar assistindo",
                emptyMessage = "Sem atividade ainda.",
                entries = continueWatching.map { it.anime to it.episodeLabel },
                onOpenAnime = onOpenAnime,
            )
        }
    }
}

@Composable
private fun LibrarySectionCard(
    title: String,
    emptyMessage: String,
    entries: List<Pair<String, String>>,
    onOpenAnime: (String) -> Unit,
) {
    GlassPanel(contentPadding = PaddingValues(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = AnimeCaosTokens.TextPrimary)
            Spacer(Modifier.weight(1f))
            Text("${entries.size}", color = AnimeCaosTokens.TextMuted, style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.height(10.dp))

        if (entries.isEmpty()) {
            Text(emptyMessage, color = AnimeCaosTokens.TextMuted)
            return@GlassPanel
        }

        entries.forEachIndexed { index, (anime, subtitle) ->
            LibraryRow(
                title = anime,
                subtitle = subtitle,
                onClick = { onOpenAnime(anime) },
            )
            if (index < entries.lastIndex) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(AnimeCaosTokens.BorderGlass.copy(alpha = 0.45f)),
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun LibraryRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(AnimeCaosTokens.SecondaryRedSoft, CircleShape),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                subtitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = AnimeCaosTokens.TextMuted,
            )
        }
        Text(
            "Abrir",
            style = MaterialTheme.typography.labelLarge,
            color = AnimeCaosTokens.SecondaryRedSoft,
        )
    }
}

@Composable
private fun GlassPanel(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(12.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && onClick != null) 0.985f else 1f,
        animationSpec = tween(durationMillis = 170),
        label = "glass_press",
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .then(
                if (onClick == null) {
                    Modifier
                } else {
                    Modifier.clickable(
                        interactionSource = interaction,
                        indication = null,
                        onClick = onClick,
                    )
                },
            ),
        color = AnimeCaosTokens.SurfaceGlass,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, AnimeCaosTokens.BorderGlass),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent,
                        ),
                    ),
                )
                .padding(contentPadding),
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun PosterCard(
    title: String,
    imageUrl: String? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    GlassPanel(
        modifier = modifier.aspectRatio(2f / 3f),
        contentPadding = PaddingValues(0.dp),
        onClick = onClick,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (imageUrl.isNullOrBlank()) {
                        Modifier.background(posterBrushFor(title))
                    } else {
                        Modifier.background(AnimeCaosTokens.SurfaceGlassStrong)
                    },
                ),
        ) {
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(62.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.62f),
                            ),
                        ),
                    )
            ) {}
            Text(
                title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
                color = AnimeCaosTokens.TextPrimary,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun GlassPosterHeader(
    title: String,
    imageUrl: String?,
) {
    GlassPanel(
        modifier = Modifier
            .fillMaxWidth()
            .height(245.dp),
        contentPadding = PaddingValues(0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AnimeCaosTokens.SurfaceGlassStrong),
        ) {
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.78f),
                            ),
                        ),
                    )
                    .padding(14.dp),
            ) {
                Column(
                    modifier = Modifier.align(Alignment.BottomStart),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.headlineMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = AnimeCaosTokens.TextPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun EpisodeRow(
    index: Int,
    title: String,
    onPlay: () -> Unit,
) {
    GlassPanel(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        onClick = onPlay,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(AnimeCaosTokens.SurfaceGlassStrong, CircleShape)
                    .border(BorderStroke(1.dp, AnimeCaosTokens.BorderGlass), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("${index + 1}", style = MaterialTheme.typography.bodyMedium)
            }
            Text(title, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Icon(Icons.Outlined.PlayArrow, contentDescription = "Assistir episodio", tint = AnimeCaosTokens.SecondaryRedSoft)
        }
    }
}
