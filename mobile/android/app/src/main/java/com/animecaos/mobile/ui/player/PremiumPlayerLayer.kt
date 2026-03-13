package com.animecaos.mobile.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.animecaos.mobile.ui.theme.AnimeCaosTokens

enum class PlayerMode {
    Fullscreen,
    Mini,
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun PremiumPlayerLayer(
    mode: PlayerMode,
    anime: String,
    episodeIndex: Int,
    episodeCount: Int,
    playerUrl: String?,
    isResolving: Boolean,
    errorMessage: String?,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onMinimize: () -> Unit,
    onExpand: () -> Unit,
    onClose: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .build()
            .apply { playWhenReady = true }
    }

    var playbackError by remember { mutableStateOf<String?>(null) }
    var lastLoadedUrl by remember { mutableStateOf<String?>(null) }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                playbackError = error.localizedMessage ?: "Falha na reproducao"
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    LaunchedEffect(playerUrl, isResolving) {
        if (isResolving) return@LaunchedEffect
        if (playerUrl.isNullOrBlank() || playerUrl == lastLoadedUrl) return@LaunchedEffect

        playbackError = null
        exoPlayer.setMediaItem(MediaItem.fromUri(playerUrl))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        lastLoadedUrl = playerUrl
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        if (!playerUrl.isNullOrBlank() && !isResolving) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { viewContext ->
                    PlayerView(viewContext).apply {
                        player = exoPlayer
                        useController = true
                        controllerAutoShow = true
                        controllerShowTimeoutMs = 2200
                        setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                        setShutterBackgroundColor(android.graphics.Color.BLACK)
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    }
                },
                update = { view ->
                    view.player = exoPlayer
                    view.useController = true
                },
            )
        } else {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = AnimeCaosTokens.SecondaryRedSoft,
            )
        }

        val effectiveError = playbackError ?: errorMessage
        if (!effectiveError.isNullOrBlank()) {
            Text(
                text = effectiveError,
                color = Color(0xFFFFD6DA),
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

