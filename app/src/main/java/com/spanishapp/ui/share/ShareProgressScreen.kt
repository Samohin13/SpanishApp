package com.spanishapp.ui.share

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val OrangePrimary = Color(0xFFFF6B1A)
private val OrangePrimary2 = Color(0xFFFF8533)

/**
 * Превью + share-меню для milestone-картинки прохождения чекпоинта.
 *
 * Открывается из CheckpointScreen.ResultView через
 * `share/{cpId}/{tier}/{percent}/{xp}/{rounds}/{minutes}`.
 *
 * UI:
 *  - вверху back-кнопка
 *  - центр: scaled-down preview сгенерированной 1080×1920 PNG
 *  - снизу две кнопки: «Поделиться» (orange filled) + «Сохранить в галерею» (outlined)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareProgressScreen(
    navController: NavHostController,
    args: ShareArgs,
    viewModel: ShareViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(args.cpId) {
        viewModel.load(args)
    }

    // Подхватываем "Saved" → показываем snackbar
    LaunchedEffect(state) {
        if (state is ShareUiState.Saved) {
            snackbar.showSnackbar((state as ShareUiState.Saved).message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Поделиться достижением") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
            )
        }
    ) { pad ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val s = state) {
                is ShareUiState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
                is ShareUiState.Error -> Text(
                    s.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                )
                is ShareUiState.Ready -> ReadyBody(
                    data = s.data,
                    onShare = { viewModel.share() },
                    onSave = { viewModel.save() },
                )
                is ShareUiState.Saved -> ReadyBody(
                    data = s.data,
                    onShare = { viewModel.share() },
                    onSave = { viewModel.save() },
                )
            }
        }
    }
}

@Composable
private fun ReadyBody(
    data: ProgressShareData,
    onShare: () -> Unit,
    onSave: () -> Unit,
) {
    val ctx = LocalContext.current

    // Рендерим bitmap один раз для превью (Default dispatcher)
    var previewBitmap by remember(data) { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(data) {
        previewBitmap = withContext(Dispatchers.Default) {
            ProgressImageRenderer.render(data)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Превью — поделись с друзьями или сохрани в галерею",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))

        // Превью в ~0.6 ширины экрана — соотношение 9:16
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .aspectRatio(1080f / 1920f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF18191C)),
            contentAlignment = Alignment.Center,
        ) {
            val bmp = previewBitmap
            if (bmp != null) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            } else {
                CircularProgressIndicator(color = OrangePrimary)
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Кнопка «Поделиться» (orange gradient) ─────────────────
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable(enabled = previewBitmap != null) { onShare() },
            color = Color.Transparent,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(listOf(OrangePrimary, OrangePrimary2))),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Поделиться",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.4.sp,
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // ── Кнопка «Сохранить в галерею» (outline) ─────────────────
        OutlinedButton(
            onClick = onSave,
            enabled = previewBitmap != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, OrangePrimary),
        ) {
            Icon(Icons.Default.Download, contentDescription = null, tint = OrangePrimary)
            Spacer(Modifier.width(8.dp))
            Text(
                "Сохранить в галерею",
                color = OrangePrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}
