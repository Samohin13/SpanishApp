package com.spanishapp.ui.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.spanishapp.R
import com.spanishapp.data.repository.LeaderboardData
import com.spanishapp.data.repository.LeaderboardEntry
import com.spanishapp.domain.algorithm.LeagueResolver
import com.spanishapp.domain.rating.CountryNames
import com.spanishapp.ui.components.LeagueBadge
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private enum class Tab { WEEK, LOCAL, WORLD }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    navController: NavHostController,
    vm: LeaderboardViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(Tab.LOCAL) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                title = {
                    Text(stringResource(R.string.lb_leaders), fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { vm.refresh() }) {
                        Icon(Icons.Default.Refresh, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (!state.optedIn) {
                OptInBlock(
                    displayName = state.displayName,
                    onJoin = { vm.optIn() },
                    onChangeName = { vm.updateDisplayName(it) }
                )
                return@Column
            }

            // Табы
            val selectedIndex = when (tab) { Tab.WEEK -> 0; Tab.LOCAL -> 1; Tab.WORLD -> 2 }
            TabRow(selectedTabIndex = selectedIndex, containerColor = Color.Transparent) {
                Tab(
                    selected = tab == Tab.WEEK,
                    onClick = {
                        tab = Tab.WEEK
                        navController.navigate("weekly_league")
                    },
                    text = { Text(stringResource(com.spanishapp.R.string.leaderboard_tab_week), fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = tab == Tab.LOCAL,
                    onClick = { tab = Tab.LOCAL },
                    text = {
                        Text(
                            CountryNames.displayWithFlag(state.deviceCountry),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                )
                Tab(
                    selected = tab == Tab.WORLD,
                    onClick = { tab = Tab.WORLD },
                    text = { Text(stringResource(R.string.lb_world_tab), fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                )
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            val data = state.data
            if (data == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.error ?: stringResource(R.string.lb_no_data), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { vm.refresh() }) { Text(stringResource(R.string.lb_refresh)) }
                    }
                }
                return@Column
            }

            // WEEK tab is just a navigation jump — fall through to LOCAL view if user lands here
            var effectiveTab = if (tab == Tab.WEEK) Tab.LOCAL else tab

            // Auto-fallback на «Мир» если в твоей стране меньше 5 юзеров.
            // Иначе юзер видит вкладку «Россия / Казахстан» с одной строкой
            // (только собой) — выглядит мёртво, нет конкуренции.
            val MIN_LOCAL_USERS = 5
            val countryEmpty = data.countryTotal < MIN_LOCAL_USERS
            if (effectiveTab == Tab.LOCAL && countryEmpty) {
                effectiveTab = Tab.WORLD
            }

            val rows = if (effectiveTab == Tab.LOCAL) data.countryRows else data.worldRows
            val myRank = if (effectiveTab == Tab.LOCAL) data.myCountryRank else data.myWorldRank
            val total = if (effectiveTab == Tab.LOCAL) data.countryTotal else data.worldTotal
            val tabName = if (effectiveTab == Tab.LOCAL) CountryNames.nameOf(state.deviceCountry) else stringResource(R.string.lb_world)

            // Подсказка пользователю что мы временно показываем мировой топ
            if (tab == Tab.LOCAL && countryEmpty) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        "В твоей стране пока мало игроков (${data.countryTotal}). " +
                        "Показываем мировой рейтинг — присоединяйся к топу!",
                        modifier = Modifier.padding(10.dp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        lineHeight = 16.sp,
                    )
                }
            }

            // Шапка с моим рангом
            if (myRank != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                ) {
                    Text(
                        stringResource(R.string.lb_my_rank, myRank, total, tabName),
                        modifier = Modifier.padding(12.dp),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (rows.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.lb_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Подиум
                    item { Podium(rows = rows.take(3), myUid = data.myUid) }
                    item { Spacer(Modifier.height(8.dp)) }
                    items(rows.drop(3), key = { it.uid }) { entry ->
                        val rank = rows.indexOf(entry) + 1
                        LeaderRow(rank = rank, entry = entry, isSelf = entry.uid == data.myUid)
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                    item {
                        TextButton(onClick = { vm.optOut() }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.lb_opt_out), color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OptInBlock(
    displayName: String,
    onJoin: () -> Unit,
    onChangeName: (String) -> Unit
) {
    var name by remember { mutableStateOf(displayName) }
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🌍", fontSize = 56.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(R.string.lb_join_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(R.string.lb_join_explain),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it.take(20) },
            label = { Text(stringResource(R.string.lb_nickname)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                if (name.isNotBlank() && name != displayName) onChangeName(name)
                onJoin()
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = name.isNotBlank()
        ) {
            Text(stringResource(R.string.lb_join_button), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun Podium(rows: List<LeaderboardEntry>, myUid: String?) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            rows.getOrNull(1)?.let { PodiumSlot("🥈", it, isSelf = it.uid == myUid, modifier = Modifier.weight(1f)) }
            rows.getOrNull(0)?.let { PodiumSlot("🥇", it, isSelf = it.uid == myUid, modifier = Modifier.weight(1.1f), big = true) }
            rows.getOrNull(2)?.let { PodiumSlot("🥉", it, isSelf = it.uid == myUid, modifier = Modifier.weight(1f)) }
        }
    }
}

@Composable
private fun PodiumSlot(medal: String, entry: LeaderboardEntry, isSelf: Boolean, modifier: Modifier = Modifier, big: Boolean = false) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isSelf) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(medal, fontSize = if (big) 36.sp else 28.sp)
        Text(
            entry.nickname,
            fontSize = if (big) 14.sp else 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Text(
            "${entry.skillRating}",
            fontSize = if (big) 16.sp else 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(CountryNames.flagOf(entry.country), fontSize = 14.sp)
    }
}

@Composable
private fun LeaderRow(rank: Int, entry: LeaderboardEntry, isSelf: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelf) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "#$rank",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(40.dp)
            )
            Text(CountryNames.flagOf(entry.country), fontSize = 18.sp)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.nickname,
                    fontSize = 14.sp,
                    fontWeight = if (isSelf) FontWeight.ExtraBold else FontWeight.SemiBold,
                    maxLines = 1
                )
                LeagueBadge(league = LeagueResolver.fromTier(entry.league), compact = true)
            }
            Text(
                "${entry.skillRating}",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
