package com.spanishapp.ui.leaderboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.spanishapp.domain.algorithm.LeagueResolver
import com.spanishapp.service.WeeklyLeagueService
import com.spanishapp.service.WeeklyMember

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyLeagueScreen(
    navController: NavHostController,
    vm: WeeklyLeagueViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsState()
    var showInfo by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                title = { Text("Лига недели", fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { showInfo = true }) { Icon(Icons.Default.Info, null) }
                    IconButton(onClick = { vm.refresh() }) { Icon(Icons.Default.Refresh, null) }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                ui.isLoading && ui.members.isEmpty() ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                !ui.optedIn -> OptInBlock(onJoin = { vm.optIn() })
                else -> CohortList(ui = ui, onLeave = { vm.optOut() })
            }
        }
    }

    if (showInfo) RulesSheet(onDismiss = { showInfo = false })
}

@Composable
private fun OptInBlock(onJoin: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🏆", fontSize = 64.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            "Недельные лиги",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Каждую неделю ты соревнуешься с 30 учениками примерно твоего уровня. " +
            "Топ-7 поднимаются в лигу выше, низ-7 опускаются.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = onJoin,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Присоединиться к недельной лиге", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CohortList(ui: WeeklyLeagueUiState, onLeave: () -> Unit) {
    val tier = ui.state?.currentTier ?: 1
    val league = LeagueResolver.fromTier(tier)
    val members = ui.members
    val total = members.size.coerceAtLeast(WeeklyLeagueService.COHORT_SIZE)
    val promoCutoff = WeeklyLeagueService.PROMOTE_COUNT
    val demoCutoff = total - WeeklyLeagueService.DEMOTE_COUNT

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color(league.accentColorHex).copy(alpha = 0.14f)
            ) {
                Column(
                    Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(league.emoji, fontSize = 44.sp)
                    Text(
                        league.city,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(league.accentColorHex)
                    )
                    Text(
                        "Заканчивается через ${ui.daysRemaining} ${pluralDays(ui.daysRemaining)}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (members.isEmpty()) {
            item {
                Spacer(Modifier.height(40.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        "Ждём других участников когорты...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Sticky-ish header for promo zone
            item { ZoneHeader("Топ-${WeeklyLeagueService.PROMOTE_COUNT} — повышение", Color(0xFF2E7D32), Icons.AutoMirrored.Filled.TrendingUp) }
            items(members.take(promoCutoff), key = { it.uid }) { m ->
                MemberRow(rank = members.indexOf(m) + 1, member = m, zone = Zone.PROMO)
            }
            val mid = members.drop(promoCutoff).take((demoCutoff - promoCutoff).coerceAtLeast(0))
            if (mid.isNotEmpty()) {
                item { ZoneHeader("Удержание", MaterialTheme.colorScheme.onSurfaceVariant, null) }
                items(mid, key = { it.uid }) { m ->
                    MemberRow(rank = members.indexOf(m) + 1, member = m, zone = Zone.HOLD)
                }
            }
            val bottom = members.drop(demoCutoff.coerceAtLeast(0))
            if (bottom.isNotEmpty()) {
                item { ZoneHeader("Понижение", Color(0xFFC62828), Icons.AutoMirrored.Filled.TrendingDown) }
                items(bottom, key = { it.uid }) { m ->
                    MemberRow(rank = members.indexOf(m) + 1, member = m, zone = Zone.DEMO)
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
        item {
            TextButton(onClick = onLeave, modifier = Modifier.fillMaxWidth()) {
                Text("Выйти из недельной лиги", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private enum class Zone { PROMO, HOLD, DEMO }

@Composable
private fun ZoneHeader(label: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector?) {
    Row(
        Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (icon != null) Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun MemberRow(rank: Int, member: WeeklyMember, zone: Zone) {
    val zoneTint = when (zone) {
        Zone.PROMO -> Color(0xFF2E7D32).copy(alpha = 0.06f)
        Zone.DEMO  -> Color(0xFFC62828).copy(alpha = 0.06f)
        Zone.HOLD  -> Color.Transparent
    }
    val bg = if (member.isMe) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else zoneTint
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = bg,
        tonalElevation = if (member.isMe) 2.dp else 0.dp,
        border = if (member.isMe) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val medal = when (rank) { 1 -> "👑"; 2 -> "🥈"; 3 -> "🥉"; else -> "" }
            Text(
                "#$rank ${if (medal.isNotEmpty()) medal else ""}".trim(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(60.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                if (member.isMe) "TY (${member.nickname})" else member.nickname,
                fontSize = 14.sp,
                fontWeight = if (member.isMe) FontWeight.ExtraBold else FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            Text(
                "${member.weekXp} XP",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RulesSheet(onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Как работают недельные лиги", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("• Каждый понедельник 30 учеников вашего уровня попадают в одну когорту.", fontSize = 14.sp)
            Text("• Всю неделю набирайте XP за правильные ответы.", fontSize = 14.sp)
            Text("• В воскресенье в 23:59: топ-7 поднимаются в лигу выше, нижние 7 опускаются.", fontSize = 14.sp)
            Text("• Лиг 8: от Aldea perdida до Madrid.", fontSize = 14.sp)
            Text("• Можно выйти и вернуться в любой момент.", fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Понятно") }
        }
    }
}

private fun pluralDays(n: Int): String = when {
    n % 10 == 1 && n % 100 != 11 -> "день"
    n % 10 in 2..4 && (n % 100 < 10 || n % 100 >= 20) -> "дня"
    else -> "дней"
}
