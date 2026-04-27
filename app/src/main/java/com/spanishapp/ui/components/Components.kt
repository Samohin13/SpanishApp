package com.spanishapp.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin

// ── NAVIGATION MODELS ─────────────────────────────────────────

data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val iconSelected: ImageVector
)

val bottomNavItems = listOf(
    NavItem("home",       "Главная",  Icons.Outlined.Home,          Icons.Filled.Home),
    NavItem("flashcards", "Слова",    Icons.Outlined.Style,         Icons.Filled.Style),
    NavItem("games",      "Игры",     Icons.Outlined.SportsEsports, Icons.Filled.SportsEsports),
    NavItem("dictionary", "Словарь",  Icons.AutoMirrored.Outlined.MenuBook, Icons.AutoMirrored.Filled.MenuBook),
    NavItem("profile",    "Профиль",  Icons.Outlined.Person,        Icons.Filled.Person)
)

// ═══════════════════════════════════════════════════════════════
//  SPANISH ATOMIC NEON BACKGROUND — живые дышащие атомы
// ═══════════════════════════════════════════════════════════════

private data class Atom(
    val x: Float,          // начальная позиция 0..1
    val y: Float,
    val baseSize: Float,   // базовый радиус в dp
    val orbitR: Float,     // радиус орбиты (как далеко плавает)
    val orbitSpeedX: Float,// скорость по X (разная у каждого)
    val orbitSpeedY: Float,// скорость по Y
    val phaseX: Float,     // начальная фаза X
    val phaseY: Float,     // начальная фаза Y
    val breathSpeed: Float,// скорость дыхания
    val breathPhase: Float,// фаза дыхания
    val color: Color,
    val alpha: Float
)

private fun rnd(a: Float, b: Float) = (a + Math.random().toFloat() * (b - a))

@Composable
fun SpanishBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val baseBg = if (isDark) Color(0xFF0A0909) else Color(0xFFFCF8F0)

    // Палитра Oliva: зелёный, оранжевый, золотой + немного синего и фиолетового для глубины
    val palette = if (isDark) listOf(
        Color(0xFF76C442),  // зелёный
        Color(0xFF76C442),  // зелёный (×2 — доминирует)
        Color(0xFFFF7043),  // оранжевый
        Color(0xFFFFCF33),  // золотой
        Color(0xFF29B6F6),  // голубой акцент
        Color(0xFF7E57C2),  // фиолетовый
    ) else listOf(
        Color(0xFF8BC34A),
        Color(0xFF8BC34A),
        Color(0xFFF05A28),
        Color(0xFFF6C445),
        Color(0xFF0288D1),
        Color(0xFF7B1FA2),
    )

    // 65 атомов: крупные (фон), средние и мелкие — три слоя
    val atoms = remember {
        buildList {
            // 10 огромных (фоновый слой, очень прозрачные)
            repeat(10) {
                add(Atom(
                    x = rnd(0f, 1f), y = rnd(0f, 1f),
                    baseSize = rnd(220f, 400f),
                    orbitR = rnd(0.04f, 0.10f),
                    orbitSpeedX = rnd(0.5f, 1.2f), orbitSpeedY = rnd(0.4f, 1.1f),
                    phaseX = rnd(0f, 6.28f), phaseY = rnd(0f, 6.28f),
                    breathSpeed = rnd(0.3f, 0.7f), breathPhase = rnd(0f, 6.28f),
                    color = palette[(it * 2) % palette.size],
                    alpha = if (isDark) rnd(0.06f, 0.12f) else rnd(0.04f, 0.08f)
                ))
            }
            // 30 средних
            repeat(30) { i ->
                add(Atom(
                    x = rnd(0f, 1f), y = rnd(0f, 1f),
                    baseSize = rnd(70f, 180f),
                    orbitR = rnd(0.05f, 0.18f),
                    orbitSpeedX = rnd(0.8f, 2.5f), orbitSpeedY = rnd(0.7f, 2.2f),
                    phaseX = rnd(0f, 6.28f), phaseY = rnd(0f, 6.28f),
                    breathSpeed = rnd(0.6f, 1.6f), breathPhase = rnd(0f, 6.28f),
                    color = palette[i % palette.size],
                    alpha = if (isDark) rnd(0.12f, 0.22f) else rnd(0.07f, 0.14f)
                ))
            }
            // 25 мелких — живые, быстрые
            repeat(25) { i ->
                add(Atom(
                    x = rnd(0f, 1f), y = rnd(0f, 1f),
                    baseSize = rnd(20f, 65f),
                    orbitR = rnd(0.06f, 0.22f),
                    orbitSpeedX = rnd(1.5f, 4f), orbitSpeedY = rnd(1.5f, 3.5f),
                    phaseX = rnd(0f, 6.28f), phaseY = rnd(0f, 6.28f),
                    breathSpeed = rnd(1.2f, 3f), breathPhase = rnd(0f, 6.28f),
                    color = palette[(i + 1) % palette.size],
                    alpha = if (isDark) rnd(0.18f, 0.32f) else rnd(0.10f, 0.20f)
                ))
            }
        }
    }

    val transition = rememberInfiniteTransition(label = "bg")
    val t by transition.animateFloat(
        initialValue = 0f, targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(60000, easing = LinearEasing), RepeatMode.Restart),
        label = "t"
    )

    Box(modifier = modifier.fillMaxSize().background(baseBg)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            atoms.forEach { atom ->
                // Орбитальное синусоидальное движение (не линейное, а живое)
                val cx = atom.x * w + w * atom.orbitR * sin(t * atom.orbitSpeedX + atom.phaseX)
                val cy = atom.y * h + h * atom.orbitR * sin(t * atom.orbitSpeedY + atom.phaseY)

                // Дыхание — каждый атом в своём ритме
                val breath = 1f + 0.28f * sin(t * atom.breathSpeed + atom.breathPhase)
                val radius = atom.baseSize.dp.toPx() * breath

                // Мягкий радиальный градиент — центр ярче, края прозрачные
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            atom.color.copy(alpha = atom.alpha),
                            atom.color.copy(alpha = atom.alpha * 0.4f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = radius.coerceAtLeast(1f)
                    ),
                    radius = radius.coerceAtLeast(1f),
                    center = Offset(cx, cy)
                )
            }
        }
        content()
    }
}

private fun ClosedRange<Int>.random() = (Math.random() * (endInclusive - start) + start).toInt()
private fun ClosedRange<Float>.random() = (Math.random() * (endInclusive - start) + start).toFloat()

// ═══════════════════════════════════════════════════════════════
//  BOTTOM BAR — крупные объёмные иконки
// ═══════════════════════════════════════════════════════════════
@Composable
fun SpanishBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val green = Color(0xFF4CAF50)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        // Подложка бара — тёмная/светлая капсула с тенью
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(28.dp),
            color    = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.92f),
            shadowElevation = 16.dp,
            tonalElevation  = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                bottomNavItems.forEach { item ->
                    val selected = currentRoute.startsWith(item.route)

                    // Плавная анимация масштаба при выборе
                    val scale by animateFloatAsState(
                        targetValue = if (selected) 1.15f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "scale_${item.route}"
                    )

                    val iconColor = if (selected) green
                                   else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onNavigate(item.route) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                        ) {
                            // Пилюля-подсветка вокруг иконки при выборе
                            Box(
                                modifier = Modifier
                                    .then(
                                        if (selected) Modifier
                                            .background(
                                                green.copy(alpha = 0.15f),
                                                RoundedCornerShape(16.dp)
                                            )
                                            .padding(horizontal = 14.dp, vertical = 7.dp)
                                        else Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector    = if (selected) item.iconSelected else item.icon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(if (selected) 32.dp else 28.dp),
                                    tint     = iconColor
                                )
                            }

                            // Подпись
                            Text(
                                text  = item.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = iconColor,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun XpProgressBar(level: Int, progress: Float, totalXp: Int, modifier: Modifier = Modifier) {
    val animProgress by animateFloatAsState(targetValue = progress.coerceIn(0f, 1f), label = "xp")
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
            Text("$level", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Box(modifier = Modifier.weight(1f).height(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)) {
            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(animProgress).clip(CircleShape).background(Color(0xFF4CAF50)))
        }
    }
}
