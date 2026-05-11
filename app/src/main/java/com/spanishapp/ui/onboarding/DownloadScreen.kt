package com.spanishapp.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spanishapp.R
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
            downloader.syncContent()
            _finished.value = true
        }
    }

    fun retry() {
        _finished.value = false
        start()
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun DownloadScreen(
    onFinished: () -> Unit,
    viewModel: DownloadViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) { viewModel.start() }
    BackHandler(enabled = true) { /* Block back — content download is mandatory. */ }

    rememberDownloadMusic()

    // Stable shuffled fact pool — same for the whole session.
    val factPool = remember { SpainFacts.rotation() }
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { factPool.size })

    val state by viewModel.state.collectAsStateWithLifecycle()
    val finished by viewModel.finished.collectAsStateWithLifecycle()

    LaunchedEffect(finished, state) {
        if (finished && state is DownloadState.Done) onFinished()
    }

    val orange = Color(0xFFFF7A2E)
    val deep   = Color(0xFFA53210)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(0f to orange, 1f to deep)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top empty space — pushes logo down to roughly mid-upper
            Spacer(Modifier.weight(1.3f))

            // ── Logo + brand (logo bigger than wordmark) ────────
            androidx.compose.foundation.Image(
                painter = painterResource(R.drawable.ic_bull),
                contentDescription = null,
                modifier = Modifier.size(150.dp),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "ESPEAK",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 2.sp,
            )

            Spacer(Modifier.height(24.dp))

            // ── Facts (just below the brand) ───────────────────
            if (state !is DownloadState.Failed) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🇪🇸", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "ЗНАЕШЬ ЛИ ТЫ?",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White.copy(alpha = 0.85f),
                        letterSpacing = 1.5.sp,
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "${pagerState.currentPage % SpainFacts.all.size + 1}/${SpainFacts.all.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f),
                    )
                }
                Spacer(Modifier.height(14.dp))
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth(),
                ) { page ->
                    Text(
                        text = factPool[page],
                        fontSize = 16.sp,
                        color = Color.White,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "← листай факты →",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.55f),
                    letterSpacing = 1.sp,
                )
            }

            // ── Bottom: failure | progress ──────────────────────
            Spacer(Modifier.weight(1f))

            when (val s = state) {
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
                        color = Color.White.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                    )
                }

                else -> {
                    // Current pack name + live percent (no frame, per owner)
                    val packLabel: String
                    val packPct: Int
                    val totalPct: Float
                    val bytesDone: Long
                    val bytesTotal: Long
                    val bps: Long
                    when (s) {
                        is DownloadState.Downloading -> {
                            packLabel = "${s.currentDisplayName}  ·  пакет ${s.packIndex}/${s.packCount}"
                            packPct = if (s.packBytesTotal > 0)
                                (s.packBytesDone * 100 / s.packBytesTotal).toInt() else 0
                            totalPct = if (s.totalBytesTotal > 0)
                                s.totalBytesDone.toFloat() / s.totalBytesTotal else 0f
                            bytesDone = s.totalBytesDone
                            bytesTotal = s.totalBytesTotal
                            bps = s.bytesPerSecond
                        }
                        else -> {
                            packLabel = "Подключаемся к серверу…"
                            packPct = 0
                            totalPct = 0f
                            bytesDone = 0L
                            bytesTotal = 0L
                            bps = 0L
                        }
                    }

                    // Pack info — borderless, lives above the progress bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            packLabel,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            "$packPct %",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.height(8.dp))

                    // Total progress bar (lives)
                    LinearProgressIndicator(
                        progress = { totalPct },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.22f),
                    )

                    Spacer(Modifier.height(14.dp))

                    // Bottom: total / speed (real device-measured)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            "Загружено: ${formatBytes(bytesDone)} из ${formatBytes(bytesTotal)}",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                        )
                        Text(
                            "${formatSpeed(bps)}",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

private fun formatBytes(b: Long): String = when {
    b < 1024 -> "$b Б"
    b < 1024 * 1024 -> "%.0f КБ".format(b / 1024.0)
    else -> "%.2f МБ".format(b / (1024.0 * 1024.0))
}

private fun formatSpeed(bps: Long): String = when {
    bps <= 0 -> "—"
    bps < 1024 -> "$bps Б/с"
    bps < 1024 * 1024 -> "%.0f КБ/с".format(bps / 1024.0)
    else -> "%.2f МБ/с".format(bps / (1024.0 * 1024.0))
}
