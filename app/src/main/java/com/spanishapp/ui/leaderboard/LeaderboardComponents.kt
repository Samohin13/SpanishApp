package com.spanishapp.ui.leaderboard

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ═══════════════════════════════════════════════════════════
//  PALETTE для leaderboard redesign (точно из mockup HTML)
// ═══════════════════════════════════════════════════════════
internal val LbBg          = Color(0xFF0B0D12)
internal val LbSurface     = Color(0xFF161922)
internal val LbSurface2    = Color(0xFF1F2330)
internal val LbSurface3    = Color(0xFF2A2F3E)
internal val LbLine        = Color(0xFF2D3344)
internal val LbText        = Color(0xFFF4F6FB)
internal val LbTextDim     = Color(0xFF9AA3B7)
internal val LbTextMute    = Color(0xFF6B7388)
internal val LbPrimary     = Color(0xFFFF6B35)
internal val LbGold        = Color(0xFFFBBF24)
internal val LbGoldDark    = Color(0xFFF59E0B)
internal val LbSilver      = Color(0xFFD1D5DB)
internal val LbSilverDark  = Color(0xFF9CA3AF)
internal val LbBronze      = Color(0xFFCD7F32)
internal val LbBronzeDark  = Color(0xFF92400E)
internal val LbGreen       = Color(0xFF4ADE80)
internal val LbRed         = Color(0xFFF87171)
internal val LbBlue        = Color(0xFF4EA1FF)
internal val LbPurple      = Color(0xFFA78BFA)
internal val LbPink        = Color(0xFFF472B6)

// Palette для аватарок — детерминированный выбор по hashCode имени
private val AVATAR_GRADIENTS = listOf(
    Brush.linearGradient(listOf(Color(0xFF4EA1FF), Color(0xFF1E6FD8))),
    Brush.linearGradient(listOf(Color(0xFFA78BFA), Color(0xFF7C3AED))),
    Brush.linearGradient(listOf(Color(0xFFF472B6), Color(0xFFDB2777))),
    Brush.linearGradient(listOf(Color(0xFF4ADE80), Color(0xFF16A34A))),
    Brush.linearGradient(listOf(Color(0xFFFBBF24), Color(0xFFF59E0B))),
    Brush.linearGradient(listOf(Color(0xFFFB7185), Color(0xFFE11D48))),
    Brush.linearGradient(listOf(Color(0xFF34D399), Color(0xFF059669))),
    Brush.linearGradient(listOf(Color(0xFF60A5FA), Color(0xFF2563EB))),
    Brush.linearGradient(listOf(Color(0xFFC084FC), Color(0xFF9333EA))),
    Brush.linearGradient(listOf(Color(0xFFFB923C), Color(0xFFEA580C))),
)

/**
 * Аватарка-кружок с первой буквой имени. Цвет детерминированный по
 * hashCode имени — один и тот же юзер всегда видит один цвет.
 */
@Composable
internal fun AvatarCircle(
    name: String,
    size: Int = 30,
    fontSize: Int = 14,
    customBrush: Brush? = null,
) {
    val initial = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val brush = customBrush ?: remember(name) {
        val idx = (name.hashCode() and 0x7FFFFFFF) % AVATAR_GRADIENTS.size
        AVATAR_GRADIENTS[idx]
    }
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(brush),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            initial,
            color = Color.White,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

// ═══════════════════════════════════════════════════════════
//  PODIUM STAGE — 3D пьедестал (silver | gold с короной | bronze)
// ═══════════════════════════════════════════════════════════
internal data class PodiumEntry(
    val name: String,
    val rating: Int,
    val uid: String,
    val flag: String? = null,
)

@Composable
internal fun PodiumStage(
    gold: PodiumEntry?,
    silver: PodiumEntry?,
    bronze: PodiumEntry?,
    myUid: String? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Silver — слева
        PodiumColumn(
            entry = silver,
            rank = 2,
            height = 155,
            ringBrush = Brush.linearGradient(listOf(LbSilver, LbSilverDark)),
            fillStart = LbSilver.copy(alpha = 0.20f),
            fillEnd = LbSilver.copy(alpha = 0.08f),
            borderColor = LbSilver.copy(alpha = 0.30f),
            pointsColor = LbSilver,
            isMe = silver?.uid == myUid,
            modifier = Modifier.weight(1f),
        )
        // Gold — по центру (выше всех)
        PodiumColumn(
            entry = gold,
            rank = 1,
            height = 200,
            ringBrush = Brush.linearGradient(listOf(LbGold, LbGoldDark)),
            fillStart = LbGold.copy(alpha = 0.30f),
            fillEnd = LbGold.copy(alpha = 0.12f),
            borderColor = LbGold.copy(alpha = 0.40f),
            pointsColor = LbGold,
            isMe = gold?.uid == myUid,
            withCrown = true,
            modifier = Modifier.weight(1.05f),
        )
        // Bronze — справа
        PodiumColumn(
            entry = bronze,
            rank = 3,
            height = 130,
            ringBrush = Brush.linearGradient(listOf(LbBronze, LbBronzeDark)),
            fillStart = LbBronze.copy(alpha = 0.20f),
            fillEnd = LbBronze.copy(alpha = 0.08f),
            borderColor = LbBronze.copy(alpha = 0.30f),
            pointsColor = LbBronze,
            isMe = bronze?.uid == myUid,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PodiumColumn(
    entry: PodiumEntry?,
    rank: Int,
    height: Int,
    ringBrush: Brush,
    fillStart: Color,
    fillEnd: Color,
    borderColor: Color,
    pointsColor: Color,
    isMe: Boolean = false,
    withCrown: Boolean = false,
    modifier: Modifier = Modifier,
) {
    if (entry == null) {
        Spacer(modifier = modifier.height(height.dp))
        return
    }
    Box(
        modifier = modifier
            .height((height + 18).dp)
            .padding(bottom = 18.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                .background(
                    Brush.verticalGradient(listOf(fillStart, fillEnd)),
                    shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp),
                )
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp),
                )
                .padding(top = 12.dp, start = 6.dp, end = 6.dp, bottom = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            if (withCrown) {
                // Корона над аватаркой
                Text(
                    "👑",
                    fontSize = 24.sp,
                    modifier = Modifier.offset(y = (-6).dp),
                )
            } else {
                Spacer(Modifier.height(18.dp))
            }
            // Аватарка с цветным градиентом ранга
            AvatarCircle(
                name = entry.name,
                size = if (withCrown) 52 else 44,
                fontSize = if (withCrown) 22 else 18,
                customBrush = ringBrush,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                entry.name,
                color = LbText,
                fontSize = if (withCrown) 12.sp else 11.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
            )
            Text(
                entry.rating.toString(),
                color = pointsColor,
                fontSize = if (withCrown) 16.sp else 14.sp,
                fontWeight = FontWeight.Black,
            )
            if (entry.flag != null) {
                Text(entry.flag, fontSize = 14.sp)
            }
        }
        // Номер ранга — кружок снизу
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(32.dp)
                .clip(CircleShape)
                .background(LbBg)
                .border(2.dp, pointsColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "#$rank",
                color = pointsColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
            )
        }
        if (isMe) {
            // v1.25.87: подсветка «ты» — рамка на ВСЮ карточку (а не только сверху).
            // Раньше padding(bottom=18) обрезал низ и скругление было только
            // сверху → визуально выглядело как обломанная коробка.
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(2.dp, LbPrimary, RoundedCornerShape(14.dp))
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  COUNTDOWN PILL — таймер обратного отсчёта с пульсом
// ═══════════════════════════════════════════════════════════
@Composable
internal fun CountdownPill(text: String, accent: Color = LbBlue) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.4f))
            .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PulseDot(color = accent)
        Text(text, color = accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
internal fun PulseDot(color: Color, size: Int = 6) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse-alpha",
    )
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha))
            .shadow(elevation = 4.dp, shape = CircleShape, clip = false, ambientColor = color, spotColor = color),
    )
}

// ═══════════════════════════════════════════════════════════
//  LIVE RIBBON — «247 игроков онлайн прямо сейчас»
// ═══════════════════════════════════════════════════════════
@Composable
internal fun LiveRibbon(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(LbGreen.copy(alpha = 0.10f))
            .border(1.dp, LbGreen.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PulseDot(color = LbGreen, size = 8)
        Text(
            text,
            color = LbGreen,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
        )
    }
}

// ═══════════════════════════════════════════════════════════
//  ZONE PILL — плашка зоны для Weekly League
//  (PROMO / HOLD / DEMO)
// ═══════════════════════════════════════════════════════════
internal enum class ZoneKind { UP, HOLD, DOWN }

@Composable
internal fun ZonePill(
    kind: ZoneKind,
    label: String,
    count: String,
    modifier: Modifier = Modifier,
) {
    val (icon, accent, bg, border) = when (kind) {
        ZoneKind.UP   -> Quad("↑", LbGreen, LbGreen.copy(alpha = 0.08f), LbGreen.copy(alpha = 0.35f))
        ZoneKind.HOLD -> Quad("═", LbTextDim, LbSurface, LbLine)
        ZoneKind.DOWN -> Quad("↓", LbRed, LbRed.copy(alpha = 0.06f), LbRed.copy(alpha = 0.25f))
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .padding(horizontal = 6.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(icon, color = accent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        Text(
            label.uppercase(),
            color = accent,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
        )
        Spacer(Modifier.height(2.dp))
        Text(count, color = LbText, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
    }
}

// Helper для деструктуризации quad
private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

// ═══════════════════════════════════════════════════════════
//  ZONE DIVIDER — разделитель секций в WeeklyLeague
// ═══════════════════════════════════════════════════════════
@Composable
internal fun ZoneDivider(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Brush.horizontalGradient(listOf(color, Color.Transparent)))
        )
        Text(
            text.uppercase(),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Brush.horizontalGradient(listOf(Color.Transparent, color)))
        )
    }
}

// ═══════════════════════════════════════════════════════════
//  MEMBER ROW — общая строка для weekly/local/world
// ═══════════════════════════════════════════════════════════
@Composable
internal fun MemberRowNew(
    rank: Int,
    name: String,
    rightValue: String,
    isMe: Boolean,
    medal: String? = null,
    flag: String? = null,
    /** v1.23.0: корона 👑 справа от ника PRO-юзеров. Сейчас (Фаза 2) показывается
     *  только для собственного ника. Для других юзеров появится в Фазе 5 после
     *  интеграции Billing + Firestore sync поля isPro. */
    isPro: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (isMe) Color.Transparent else LbSurface,
    ) {
        Row(
            modifier = Modifier
                .then(
                    if (isMe) Modifier
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    LbPrimary.copy(alpha = 0.20f),
                                    LbPrimary.copy(alpha = 0.05f),
                                )
                            ),
                            shape = RoundedCornerShape(12.dp),
                        )
                        .border(1.5.dp, LbPrimary, RoundedCornerShape(12.dp))
                    else Modifier
                )
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                rank.toString(),
                color = LbTextDim,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.width(28.dp),
            )
            if (medal != null) {
                Text(medal, fontSize = 18.sp, modifier = Modifier.width(24.dp))
            }
            AvatarCircle(name = name, size = 30, fontSize = 14)
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    if (isMe) "$name (ты)" else name,
                    color = LbText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                if (isPro) {
                    Text("👑", fontSize = 11.sp)
                }
            }
            if (flag != null) {
                Text(flag, fontSize = 14.sp)
            }
            Text(
                rightValue,
                color = LbPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}
