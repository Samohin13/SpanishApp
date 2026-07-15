package com.spanishapp.ui.games

import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private data class Game(
    val title: String,
    @androidx.annotation.StringRes val descriptionRes: Int,
    val icon: ImageVector,
    val color: Color,
    val route: String,
    val watermark: com.spanishapp.ui.home.WatermarkTheme,
    /** v1.27: фирменный вектор-логотип вместо тонированной иконки. */
    val customLogo: (@Composable (androidx.compose.ui.unit.Dp) -> Unit)? = null,
    /** v1.27: бейдж NEW на карточке. */
    val isNew: Boolean = false,
)

// Описания через @StringRes — переключаются по языку. Названия игр
// (Artículos, Rápido, etc.) — испанский бренд, не локализуем.
private val GAMES: List<Game> = listOf(
    // v1.27: новые игры — первыми, с бейджем NEW и фирменными логотипами.
    Game("Frase Loca",     com.spanishapp.R.string.game_frase_desc,     Icons.Default.Extension,  Color(0xFFFF8A3D), "game_frase",      com.spanishapp.ui.home.WatermarkTheme.GAME_FRASE,
        customLogo = { size -> FraseLocaLogo(size) }, isNew = true),
    Game("El Oído",        com.spanishapp.R.string.game_oido_desc,      Icons.Default.Hearing,    Color(0xFF4EA1FF), "game_oido",       com.spanishapp.ui.home.WatermarkTheme.GAME_OIDO,
        customLogo = { size -> ElOidoLogo(size) }, isNew = true),
    Game("Artículos",      com.spanishapp.R.string.game_articles_desc,  Icons.Default.Category,   Color(0xFF7B2FBE), "game_articles",   com.spanishapp.ui.home.WatermarkTheme.GAME_ARTICLES),
    Game("Rápido",         com.spanishapp.R.string.game_speed_desc,     Icons.Default.Timer,      Color(0xFFE040FB), "game_speed",      com.spanishapp.ui.home.WatermarkTheme.GAME_SPEED),
    Game("Verbos",         com.spanishapp.R.string.game_verbos_desc,    Icons.Default.Translate,  Color(0xFF2196F3), "conjugation_quiz",com.spanishapp.ui.home.WatermarkTheme.GAME_VERBS),
    Game("Sopa de Letras", com.spanishapp.R.string.game_sopa_desc,      Icons.Default.GridOn,     Color(0xFF4CAF50), "game_sopa",       com.spanishapp.ui.home.WatermarkTheme.GAME_SOPA),
    Game("Palabra Maestra",com.spanishapp.R.string.game_palabra_desc,   Icons.Default.TextFields, Color(0xFFFF9500), "game_palabra",    com.spanishapp.ui.home.WatermarkTheme.GAME_PALABRA),
    Game("Cálculo",        com.spanishapp.R.string.game_math_desc,      Icons.Default.Calculate,  Color(0xFFF44336), "game_math",       com.spanishapp.ui.home.WatermarkTheme.GAME_MATH),
    Game("Crucigrama",     com.spanishapp.R.string.game_crossword_desc, Icons.Default.BorderAll,  Color(0xFF26A69A), "game_crossword",  com.spanishapp.ui.home.WatermarkTheme.GAME_CROSSWORD),
    Game("Libros",         com.spanishapp.R.string.game_libros_desc,    Icons.AutoMirrored.Filled.MenuBook,   Color(
        0xFFBEA62F
    ), "game_libros", com.spanishapp.ui.home.WatermarkTheme.GAME_LIBROS)
)

@Composable
fun GamesScreen(
    navController: NavHostController,
    vm: GamesScreenViewModel = hiltViewModel()
) {
    val progress by vm.gameProgress.collectAsStateWithLifecycle()
    val isPro by com.spanishapp.ui.games.common.rememberIsProState()
    // v1.13.2: 2 cols ВЕЗДЕ (юзер: "должно быть 2 столбца 4 строки
    // как в мобильной просто крупнее"). Раньше было 4 cols × 2 rows
    // на планшете — выглядело как «сетка чисел», карточки маленькие.
    // 2 cols + adaptive heightIn = крупные читаемые карточки.
    val cols = 2
    val isWide = com.spanishapp.ui.adaptive.isWideScreen()

    LazyVerticalGrid(
        columns = GridCells.Fixed(cols),
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(cols) }) {
            Column {
                Text(
                    androidx.compose.ui.res.stringResource(com.spanishapp.R.string.games_title),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
            }
        }

        // v1.23.0: PRO promo баннер на хабе игр (только для free).
        // Юзер видит «открой все 100 уровней» прямо над сеткой игр —
        // самая важная точка для конверсии в играх.
        if (!isPro) {
            item(span = { GridItemSpan(cols) }) {
                GamesProBanner(onClick = {
                    navController.navigate("paywall") { launchSingleTop = true }
                })
            }
        }

        itemsIndexed(GAMES, key = { _, g -> g.route }) { idx, g ->
            // Staggered entrance: each card slides in 60ms after the previous.
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(60L * idx)
                visible = true
            }
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                    initialOffsetY = { it / 3 }
                ) + fadeIn(animationSpec = tween(300))
            ) {
                GameCard(
                    game = g,
                    progress = progress[g.route],
                    onClick = { navController.navigate(g.route) },
                    wobblePhase = (idx * 0.37f) % 1f
                )
            }
        }
    }
}

@Composable
private fun GameCard(
    game: Game,
    progress: GameProgressInfo?,
    onClick: () -> Unit,
    wobblePhase: Float = 0f
) {
    // Continuous micro-wobble: -3° ↔ +3° over 2.5s, phase-shifted per card so
    // they don't sway in lock-step.
    val infinite = rememberInfiniteTransition(label = "wobble_${game.route}")
    val wobble by infinite.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset((wobblePhase * 2500).toInt())
        ),
        label = "wobble_angle_${game.route}"
    )

    // v1.14.2: квадратнее (юзер: "карточки игр более пропорциональными,
    // более квадратными"). Было height fixed 220dp = aspect 1.6:1 wide.
    // aspectRatio(1f) даёт 280×280dp на 2 cols × 600dp экрана.
    val isWide = com.spanishapp.ui.adaptive.isWideScreen()
    val cardModifier = if (isWide) {
        Modifier.fillMaxWidth().aspectRatio(1f)
    } else {
        Modifier.fillMaxWidth().height(160.dp)
    }
    com.spanishapp.ui.components.PressableCard(
        onClick = onClick,
        modifier = cardModifier,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 4.dp
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        // Per-game thematic watermark (stopwatch / book / arrows / …)
        // sits behind the icon + title, anchored bottom-right, alpha
        // ~0.13. Same system used by Bento and Continue cards.
        com.spanishapp.ui.home.ThematicWatermark(theme = game.watermark, accent = game.color)
        // v1.13.2: adaptive sizing для карточки игры
        val cardIsWide = com.spanishapp.ui.adaptive.isWideScreen()
        val pad = if (cardIsWide) 20.dp else 14.dp
        val iconBoxSize = if (cardIsWide) 64.dp else 44.dp
        val iconSize = if (cardIsWide) 36.dp else 24.dp
        val titleFont = if (cardIsWide) 22.sp else 16.sp
        val descFont = if (cardIsWide) 15.sp else 12.sp
        val descLineHeight = if (cardIsWide) 18.sp else 14.sp
        val progFont = if (cardIsWide) 13.sp else 11.sp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (game.customLogo != null) {
                // v1.27: фирменный вектор-логотип (Frase Loca / El Oído)
                Box(modifier = Modifier.graphicsLayer { rotationZ = wobble }) {
                    game.customLogo.invoke(iconBoxSize)
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(iconBoxSize)
                        .clip(CircleShape)
                        .background(game.color.copy(alpha = 0.12f))
                        .graphicsLayer { rotationZ = wobble },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(game.icon, null, tint = game.color, modifier = Modifier.size(iconSize))
                }
            }

            Column {
                Text(
                    game.title,
                    fontSize = titleFont,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    androidx.compose.ui.res.stringResource(game.descriptionRes),
                    fontSize = descFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = descLineHeight,
                    maxLines = 2
                )
            }

            // Прогресс / лучший результат
            if (progress != null && progress.label.isNotBlank()) {
                Surface(
                    color = game.color.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        progress.label,
                        fontSize = progFont,
                        fontWeight = FontWeight.SemiBold,
                        color = game.color,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            } else {
                Spacer(Modifier.height(0.dp))
            }
        }

        // v1.27: бейдж NEW у свежих игр
        if (game.isNew) {
            Surface(
                color = Color(0xFF4ADE80),
                shape = RoundedCornerShape(99.dp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
            ) {
                Text(
                    "NEW",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = Color(0xFF052312),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
      } // Box
    }
}


// ════════════════════════════════════════════════════════════
//  v1.23.0: PRO banner на GamesScreen — заметный, призывает
//  купить чтобы открыть уровни 11-100 каждой игры.
// ════════════════════════════════════════════════════════════
@Composable
private fun GamesProBanner(onClick: () -> Unit) {
    val primary = Color(0xFFFF8A3D)
    val primary2 = Color(0xFFFF6A1A)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
            .background(
                androidx.compose.ui.graphics.Brush.linearGradient(
                    listOf(primary, primary2)
                )
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
        ) {
            Text("🎯", fontSize = 32.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Открой все 100 уровней",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    "Сейчас доступны только первые 10 каждой",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                )
            }
            Box(
                modifier = Modifier
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(99.dp))
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    "PRO 💎",
                    color = primary2,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                )
            }
        }
    }
}
