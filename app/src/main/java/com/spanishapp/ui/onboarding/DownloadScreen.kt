package com.spanishapp.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spanishapp.data.content.ContentDownloader
import com.spanishapp.data.content.DownloadState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadViewModel @Inject constructor(
    val downloader: ContentDownloader,
) : ViewModel() {

    val state: StateFlow<DownloadState> = downloader.state

    private val _finished = MutableStateFlow(false)
    val finished: StateFlow<Boolean> = _finished.asStateFlow()

    fun start() {
        viewModelScope.launch {
            val result = downloader.syncContent()
            _finished.value = true
        }
    }

    fun retry() {
        _finished.value = false
        start()
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun DownloadScreen(
    onFinished: () -> Unit,
    viewModel: DownloadViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) { viewModel.start() }
    BackHandler(enabled = true) { /* Block back — content download is mandatory. */ }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val finished by viewModel.finished.collectAsStateWithLifecycle()

    LaunchedEffect(finished, state) {
        if (finished && state is DownloadState.Done) {
            onFinished()
        }
    }

    val orange = Color(0xFFFF7A2E)
    val deep   = Color(0xFFA53210)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f   to orange,
                    1f   to deep,
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Brand
            Text(
                "ESPEAK",
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 2.sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Загрузка пакетов",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.85f),
            )

            Spacer(Modifier.height(40.dp))

            when (val s = state) {
                DownloadState.Idle, is DownloadState.FetchingManifest -> {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.2f),
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Подключаемся к серверу…",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                    )
                }

                is DownloadState.Downloading -> {
                    val pct = if (s.totalBytesTotal > 0)
                        (s.totalBytesDone.toFloat() / s.totalBytesTotal.toFloat()).coerceIn(0f, 1f)
                    else 0f

                    LinearProgressIndicator(
                        progress = { pct },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.22f),
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "${(pct * 100).toInt()} %",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Загружено: ${formatBytes(s.totalBytesDone)} из ${formatBytes(s.totalBytesTotal)}",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                    Text(
                        "Скорость: ${formatSpeed(s.bytesPerSecond)}",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f),
                    )

                    Spacer(Modifier.height(24.dp))

                    // Per-pack list
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        // We don't know all pack names here without holding the manifest.
                        // Render compact status: currently-downloading + completed counts.
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("⏳", fontSize = 18.sp)
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        s.currentDisplayName,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                    )
                                    Text(
                                        "Пакет ${s.packIndex} из ${s.packCount}",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 11.sp,
                                    )
                                }
                                Text(
                                    "${(s.packBytesDone.toFloat() / s.packBytesTotal.coerceAtLeast(1L) * 100).toInt()} %",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                )
                            }
                        }
                        if (s.completedPacks.isNotEmpty()) {
                            Text(
                                "✓ Готово: ${s.completedPacks.size} / ${s.packCount}",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp),
                            )
                        }
                    }
                }

                DownloadState.Done -> {
                    Text("✓", fontSize = 56.sp, color = Color.White)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Готово!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                }

                is DownloadState.Failed -> {
                    Text("⚠", fontSize = 48.sp, color = Color.White)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Не удалось загрузить",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        s.cause,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.75f),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.retry() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = orange,
                        ),
                    ) { Text("Повторить", fontWeight = FontWeight.Bold) }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Без загрузки контента приложение не сможет работать.",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.65f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

private fun formatBytes(b: Long): String = when {
    b < 1024 -> "$b Б"
    b < 1024 * 1024 -> "%.1f КБ".format(b / 1024.0)
    else -> "%.2f МБ".format(b / (1024.0 * 1024.0))
}

private fun formatSpeed(bps: Long): String = when {
    bps < 1024 -> "$bps Б/с"
    bps < 1024 * 1024 -> "%.0f КБ/с".format(bps / 1024.0)
    else -> "%.2f МБ/с".format(bps / (1024.0 * 1024.0))
}
