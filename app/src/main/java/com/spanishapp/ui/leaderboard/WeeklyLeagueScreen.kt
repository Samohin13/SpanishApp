package com.spanishapp.ui.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.spanishapp.R
import com.spanishapp.domain.algorithm.League
import com.spanishapp.domain.algorithm.LeagueResolver
import com.spanishapp.service.WeeklyLeagueService
import com.spanishapp.service.WeeklyMember
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyLeagueScreen(
    navController: NavHostController,
    vm: WeeklyLeagueViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    var showInfo by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = LbBg,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                title = { Text(stringResource(R.string.weekly_league_title), fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = LbText) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = LbText)
                    }
                },
                actions = {
                    IconButton(onClick = { showInfo = true }) { Icon(Icons.Default.Info, null, tint = LbText) }
                    IconButton(onClick = { vm.refresh() }) { Icon(Icons.Default.Refresh, null, tint = LbText) }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(LbBg)) {
            when {
                ui.isLoading && ui.members.isEmpty() ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = LbPrimary) }
                !ui.optedIn -> OptInBlock(onJoin = { vm.optIn() })
                else -> CohortList(ui = ui, onLeave = { vm.optOut() })
            }
        }
    }

    if (showInfo) RulesSheet(onDismiss = { showInfo = false })
}

// ═══════════════════════════════════════════════════════════
//  OPT-IN
// ═══════════════════════════════════════════════════════════
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
            stringResource(R.string.weekly_league_heading),
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = LbText,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(R.string.weekly_league_subtitle),
            fontSize = 14.sp,
            color = LbTextDim,
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = onJoin,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LbPrimary),
        ) {
            Text(stringResource(R.string.weekly_league_join), fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  COHORT LIST — главный экран с hero + zone-pills + members
// ═══════════════════════════════════════════════════════════
@Composable
private fun CohortList(ui: WeeklyLeagueUiState, onLeave: () -> Unit) {
    val tier = ui.state?.currentTier ?: 1
    val league = LeagueResolver.fromTier(tier)
    val members = ui.members

    val promoCutoff = WeeklyLeagueService.PROMOTE_COUNT
    val total = members.size
    val demoCutoff = (total - WeeklyLeagueService.DEMOTE_COUNT).coerceAtLeast(promoCutoff)

    LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // ── HERO с цветным градиентом лиги ──
        item { WeekHero(league = league, daysRemaining = ui.daysRemaining, totalTiers = WeeklyLeagueService.MAX_TIER) }

        // ── Zone-pills strip (3 плашки) ──
        item {
            ZonePillBar(
                promoteCount = WeeklyLeagueService.PROMOTE_COUNT,
                demoteCount = WeeklyLeagueService.DEMOTE_COUNT,
                cohortSize = WeeklyLeagueService.COHORT_SIZE,
                nextLeague = LeagueResolver.next(league),
                prevLeague = LeagueResolver.LEAGUES.firstOrNull { it.tier == league.tier - 1 },
            )
        }

        if (members.isEmpty()) {
            item {
                Spacer(Modifier.height(40.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.weekly_league_waiting_cohort),
                        color = LbTextDim,
                    )
                }
            }
        } else {
            // ── PROMO zone (вверх в Zaragoza) ──
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    ZoneDivider(
                        text = "В повышение · Топ ${WeeklyLeagueService.PROMOTE_COUNT}",
                        color = LbGreen,
                    )
                }
            }
            items(members.take(promoCutoff), key = { it.uid }) { m ->
                val rank = members.indexOf(m) + 1
                MemberRowFromWeekly(rank = rank, member = m)
            }

            // ── HOLD zone (середина) ──
            val mid = members.drop(promoCutoff).take((demoCutoff - promoCutoff).coerceAtLeast(0))
            if (mid.isNotEmpty()) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        ZoneDivider(text = "Зона удержания", color = LbTextDim)
                    }
                }
                items(mid, key = { it.uid }) { m ->
                    val rank = members.indexOf(m) + 1
                    MemberRowFromWeekly(rank = rank, member = m)
                }
            }

            // ── DEMO zone (вниз в Santiago) ──
            val bottom = members.drop(demoCutoff)
            if (bottom.isNotEmpty()) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        ZoneDivider(text = "Вылет вниз", color = LbRed)
                    }
                }
                items(bottom, key = { it.uid }) { m ->
                    val rank = members.indexOf(m) + 1
                    MemberRowFromWeekly(rank = rank, member = m)
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
        item {
            TextButton(
                onClick = onLeave,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.weekly_league_leave), color = LbRed)
            }
        }
    }
}

@Composable
private fun MemberRowFromWeekly(rank: Int, member: WeeklyMember) {
    val medal = when (rank) {
        1 -> "👑"
        2 -> "🥈"
        3 -> "🥉"
        else -> null
    }
    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) {
        MemberRowNew(
            rank = rank,
            name = member.nickname,
            rightValue = "${member.weekXp} XP",
            isMe = member.isMe,
            medal = medal,
        )
    }
}

// ═══════════════════════════════════════════════════════════
//  WEEK HERO — цветной hero лиги с countdown
// ═══════════════════════════════════════════════════════════
@Composable
private fun WeekHero(league: League, daysRemaining: Int, totalTiers: Int) {
    val accent = Color(league.accentColorHex)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        accent.copy(alpha = 0.30f),
                        accent.copy(alpha = 0.10f),
                        Color.Transparent,
                    ),
                    radius = 800f,
                )
            )
            .padding(top = 8.dp, bottom = 20.dp),
    ) {
        // Watermark — большая полупрозрачная иконка лиги на фоне
        Text(
            league.emoji,
            fontSize = 280.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-40).dp)
                .alpha(0.04f),
        )
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // «Лига 3 из 8»
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(24.dp).height(1.dp).background(accent.copy(alpha = 0.5f)))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Лига ${league.tier} из $totalTiers".uppercase(),
                    color = accent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                )
                Spacer(Modifier.width(8.dp))
                Box(modifier = Modifier.width(24.dp).height(1.dp).background(accent.copy(alpha = 0.5f)))
            }
            Spacer(Modifier.height(6.dp))
            // Большой emoji города
            Text(league.emoji, fontSize = 72.sp)
            Spacer(Modifier.height(4.dp))
            // Название города
            Text(
                league.city,
                color = accent,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp,
            )
            Spacer(Modifier.height(2.dp))
            // Диапазон рейтинга
            Text(
                "${league.ratingFrom} – ${league.ratingTo} XP".uppercase(),
                color = LbTextDim,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp,
            )
            Spacer(Modifier.height(14.dp))
            // Countdown
            CountdownPill(
                text = "До конца недели · ${daysRemaining} ${pluralDays(daysRemaining)}",
                accent = accent,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  ZONE-PILL BAR — 3 плашки сверху списка
// ═══════════════════════════════════════════════════════════
@Composable
private fun ZonePillBar(
    promoteCount: Int,
    demoteCount: Int,
    cohortSize: Int,
    nextLeague: League?,
    prevLeague: League?,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ZonePill(
            kind = ZoneKind.UP,
            label = if (nextLeague != null) "В ${nextLeague.city}" else "Топ",
            count = "Топ $promoteCount",
            modifier = Modifier.weight(1f),
        )
        ZonePill(
            kind = ZoneKind.HOLD,
            label = "Удержание",
            count = "${promoteCount + 1}–${cohortSize - demoteCount}",
            modifier = Modifier.weight(1f),
        )
        ZonePill(
            kind = ZoneKind.DOWN,
            label = if (prevLeague != null) "Вниз в ${prevLeague.city}" else "Низ",
            count = "Снизу $demoteCount",
            modifier = Modifier.weight(1f),
        )
    }
}

// ═══════════════════════════════════════════════════════════
//  RULES SHEET
// ═══════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RulesSheet(onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = LbSurface) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(R.string.weekly_league_how_works), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LbText)
            Text(stringResource(R.string.weekly_league_rule_1), fontSize = 14.sp, color = LbTextDim)
            Text(stringResource(R.string.weekly_league_rule_2), fontSize = 14.sp, color = LbTextDim)
            Text(stringResource(R.string.weekly_league_rule_3), fontSize = 14.sp, color = LbTextDim)
            Text(stringResource(R.string.weekly_league_rule_4), fontSize = 14.sp, color = LbTextDim)
            Text(stringResource(R.string.weekly_league_rule_5), fontSize = 14.sp, color = LbTextDim)
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = LbPrimary),
            ) {
                Text(stringResource(R.string.weekly_league_understood), color = Color.White)
            }
        }
    }
}

private fun pluralDays(n: Int): String = when {
    n % 10 == 1 && n % 100 != 11 -> "день"
    n % 10 in 2..4 && (n % 100 < 10 || n % 100 >= 20) -> "дня"
    else -> "дней"
}
