package com.spanishapp.ui.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.spanishapp.R
import com.spanishapp.data.repository.CountryAggregate
import com.spanishapp.data.repository.LeaderboardData
import com.spanishapp.data.repository.LeaderboardEntry
import com.spanishapp.domain.rating.CountryNames
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private enum class Tab { WEEK, LOCAL, WORLD }
private enum class WorldSegment { COUNTRIES, PLAYERS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    navController: NavHostController,
    vm: LeaderboardViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val isMePro by vm.isMePro.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(Tab.LOCAL) }
    var worldSegment by remember { mutableStateOf(WorldSegment.COUNTRIES) }

    Scaffold(
        containerColor = LbBg,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                title = {
                    Text(stringResource(R.string.lb_leaders), fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = LbText)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = LbText)
                    }
                },
                actions = {
                    IconButton(onClick = { vm.refresh() }) {
                        Icon(Icons.Default.Refresh, null, tint = LbText)
                    }
                }
            )
        }
    ) { padding ->
        if (state.needsNamePrompt) {
            NamePromptDialog(
                initialName = state.displayName,
                onConfirm = { vm.updateDisplayName(it) },
                onDismiss = { vm.dismissNamePrompt() }
            )
        }
        // v1.26.1 (Model B): гость нажал «участвовать» — нужен аккаунт.
        if (state.needsAccount) {
            AlertDialog(
                onDismissRequest = { vm.consumeNeedsAccount() },
                title = { Text(stringResource(com.spanishapp.R.string.guest_leaderboard_gate_title)) },
                text = { Text(stringResource(com.spanishapp.R.string.guest_leaderboard_gate_body)) },
                confirmButton = {
                    TextButton(onClick = {
                        vm.consumeNeedsAccount()
                        navController.navigate("register") { launchSingleTop = true }
                    }) { Text(stringResource(com.spanishapp.R.string.guest_leaderboard_gate_cta)) }
                },
                dismissButton = {
                    TextButton(onClick = { vm.consumeNeedsAccount() }) {
                        Text(stringResource(com.spanishapp.R.string.auth_back))
                    }
                }
            )
        }
        if (state.showCountryPicker) {
            CountryPickerDialog(
                currentIso = state.deviceCountry,
                onSelect = { vm.setCountryOverride(it) },
                onDismiss = { vm.dismissCountryPicker() }
            )
        }
        Column(modifier = Modifier.fillMaxSize().background(LbBg).padding(padding)) {
            if (!state.optedIn) {
                OptInBlock(
                    displayName = state.displayName,
                    onJoin = { vm.optIn() },
                    onChangeName = { vm.updateDisplayName(it) }
                )
                return@Column
            }

            // ── Tabs ──
            val selectedIndex = when (tab) { Tab.WEEK -> 0; Tab.LOCAL -> 1; Tab.WORLD -> 2 }
            TabRow(
                selectedTabIndex = selectedIndex,
                containerColor = LbBg,
                contentColor = LbPrimary,
            ) {
                Tab(
                    selected = tab == Tab.WEEK,
                    onClick = {
                        tab = Tab.WEEK
                        navController.navigate("weekly_league")
                    },
                    selectedContentColor = LbPrimary,
                    unselectedContentColor = LbTextDim,
                    // v1.23.5 (audit fix): эмодзи 🏆 уже в strings.xml
                    // ("Неделя 🏆") — раньше код добавлял ещё одну → дубликат.
                    text = { Text(stringResource(R.string.leaderboard_tab_week), fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = tab == Tab.LOCAL,
                    onClick = { tab = Tab.LOCAL },
                    selectedContentColor = LbPrimary,
                    unselectedContentColor = LbTextDim,
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
                    selectedContentColor = LbPrimary,
                    unselectedContentColor = LbTextDim,
                    // v1.23.5 (audit fix): эмодзи 🌍 уже в strings.xml
                    // ("🌍 Мир") — раньше код добавлял ещё одну → две планеты.
                    text = { Text(stringResource(R.string.lb_world_tab), fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                )
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LbPrimary)
                }
                return@Column
            }

            val data = state.data
            if (data == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.error ?: stringResource(R.string.lb_no_data), color = LbTextDim)
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { vm.refresh() },
                            colors = ButtonDefaults.buttonColors(containerColor = LbPrimary),
                        ) { Text(stringResource(R.string.lb_refresh), color = Color.White) }
                    }
                }
                return@Column
            }

            // Auto-fallback на WORLD если в стране меньше 5 юзеров
            val MIN_LOCAL_USERS = 5
            val effectiveTab = if (tab == Tab.LOCAL && data.countryTotal < MIN_LOCAL_USERS) Tab.WORLD else tab

            when (effectiveTab) {
                Tab.LOCAL -> LocalView(data = data, state = state, isMePro = isMePro, onCountryPicker = { vm.showCountryPicker() }, onOptOut = { vm.optOut() })
                Tab.WORLD -> WorldView(
                    data = data,
                    segment = worldSegment,
                    isMePro = isMePro,
                    onSegmentChange = { worldSegment = it },
                    onOptOut = { vm.optOut() },
                )
                else -> Unit
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  LOCAL VIEW — Казахстан hero + пьедестал + список
// ═══════════════════════════════════════════════════════════
@Composable
private fun LocalView(
    data: LeaderboardData,
    state: LeaderboardUiState,
    isMePro: Boolean,
    onCountryPicker: () -> Unit,
    onOptOut: () -> Unit,
) {
    val rows = data.countryRows
    val gold = rows.getOrNull(0)?.toPodium()
    val silver = rows.getOrNull(1)?.toPodium()
    val bronze = rows.getOrNull(2)?.toPodium()
    val rest = rows.drop(3)

    LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // ── Country hero ──
        item {
            LocalCountryHero(
                iso = state.deviceCountry,
                playerCount = data.countryTotal,
                worldRank = data.myCountryWorldRank,
                worldCountriesTotal = data.worldCountriesCount,
                onChangeCountry = onCountryPicker,
            )
        }

        // ── Подиум 3D ──
        if (gold != null) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
                    PodiumStage(
                        gold = gold,
                        silver = silver,
                        bronze = bronze,
                        myUid = data.myUid,
                    )
                }
            }
        }

        // ── My rank pill ──
        val myRank = data.myCountryRank
        if (myRank != null) {
            item { MyRankPill(rank = myRank, total = data.countryTotal, where = CountryNames.nameOf(state.deviceCountry)) }
        }

        // ── Остальной список ──
        if (rest.isNotEmpty()) {
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    ZoneDivider(text = "Топ ${rest.size + 3}", color = LbTextMute)
                }
            }
            itemsIndexed(rest, key = { _, it -> it.uid }) { idx, entry ->
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) {
                    MemberRowNew(
                        rank = idx + 4,
                        name = entry.nickname,
                        rightValue = entry.skillRating.toString(),
                        isMe = entry.uid == data.myUid,
                        flag = CountryNames.flagOf(entry.country),
                        isPro = entry.uid == data.myUid && isMePro,
                    )
                }
            }
        }

        item { Spacer(Modifier.height(20.dp)) }
        item {
            TextButton(onClick = onOptOut, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.lb_opt_out), color = LbRed)
            }
        }
    }
}

@Composable
private fun LocalCountryHero(
    iso: String,
    playerCount: Int,
    worldRank: Int?,
    worldCountriesTotal: Int,
    onChangeCountry: () -> Unit,
) {
    // Зелёно-жёлтый градиент как фон + большой флаг + трофей справа
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF00AF8F).copy(alpha = 0.30f),
                        Color(0xFF00AF8F).copy(alpha = 0.05f),
                        Color.Transparent,
                    )
                )
            )
            .padding(18.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                CountryNames.flagOf(iso),
                fontSize = 56.sp,
                modifier = Modifier.alpha(0.95f),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    CountryNames.nameOf(iso),
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                )
                Spacer(Modifier.height(2.dp))
                val rankText = if (worldRank != null && worldCountriesTotal > 1)
                    "$playerCount игроков · #$worldRank из $worldCountriesTotal по рейтингу"
                else
                    "$playerCount игроков"
                Text(
                    rankText,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            // Трофей справа — если страна в топ-3
            if (worldRank != null && worldRank <= 3) {
                val (medal, label) = when (worldRank) {
                    1 -> "🥇" to "ЛИДЕР"
                    2 -> "🥈" to "СЕРЕБРО"
                    else -> "🥉" to "БРОНЗА"
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(listOf(LbGold, LbGoldDark)))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(medal, fontSize = 16.sp)
                        Text(
                            label,
                            color = Color(0xFF1A1300),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        // Pill для смены страны
        CountryChangePill(iso = iso, onClick = onChangeCountry)
    }
}

@Composable
private fun CountryChangePill(iso: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(LbGreen.copy(alpha = 0.10f))
            .border(1.dp, LbGreen.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(CountryNames.flagOf(iso), fontSize = 18.sp)
        Text(
            "Твоя страна: ${CountryNames.nameOf(iso)}",
            color = LbTextDim,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f),
        )
        Icon(Icons.Default.Edit, contentDescription = null, tint = LbGreen, modifier = Modifier.size(16.dp))
    }
}

// ═══════════════════════════════════════════════════════════
//  WORLD VIEW — globe hero + live ribbon + segment + board
// ═══════════════════════════════════════════════════════════
@Composable
private fun WorldView(
    data: LeaderboardData,
    segment: WorldSegment,
    isMePro: Boolean,
    onSegmentChange: (WorldSegment) -> Unit,
    onOptOut: () -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item { WorldGlobeHero(playerTotal = data.worldTotal, countriesTotal = data.worldCountriesCount) }

        item {
            // Live ribbon
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                LiveRibbon(text = "ИГРАЕТ В ИСПАНСКИЙ ПРЯМО СЕЙЧАС", modifier = Modifier.fillMaxWidth())
            }
        }

        item { WorldSegmentSwitch(segment = segment, onChange = onSegmentChange) }

        when (segment) {
            WorldSegment.COUNTRIES -> {
                if (data.countriesAggregate.isEmpty()) {
                    item { EmptyState("Пока недостаточно данных для рейтинга стран") }
                } else {
                    itemsIndexed(data.countriesAggregate, key = { _, c -> c.iso }) { idx, country ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 3.dp)) {
                            CountryAggregateRow(
                                rank = idx + 1,
                                country = country,
                                isMyCountry = country.iso == data.country,
                            )
                        }
                    }
                }
            }
            WorldSegment.PLAYERS -> {
                val rows = data.worldRows
                val rest = rows.drop(3)
                val gold = rows.getOrNull(0)?.toPodium()
                val silver = rows.getOrNull(1)?.toPodium()
                val bronze = rows.getOrNull(2)?.toPodium()

                if (gold != null) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
                            PodiumStage(gold = gold, silver = silver, bronze = bronze, myUid = data.myUid)
                        }
                    }
                }
                val myRank = data.myWorldRank
                if (myRank != null) {
                    item { MyRankPill(rank = myRank, total = data.worldTotal, where = "мире") }
                }
                if (rest.isNotEmpty()) {
                    item {
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            ZoneDivider(text = "Топ ${rest.size + 3}", color = LbTextMute)
                        }
                    }
                    itemsIndexed(rest, key = { _, it -> it.uid }) { idx, entry ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) {
                            MemberRowNew(
                                rank = idx + 4,
                                name = entry.nickname,
                                rightValue = entry.skillRating.toString(),
                                isMe = entry.uid == data.myUid,
                                flag = CountryNames.flagOf(entry.country),
                                isPro = entry.uid == data.myUid && isMePro,
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(20.dp)) }
        item {
            TextButton(onClick = onOptOut, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.lb_opt_out), color = LbRed)
            }
        }
    }
}

@Composable
private fun WorldGlobeHero(playerTotal: Int, countriesTotal: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        LbBlue.copy(alpha = 0.25f),
                        LbBlue.copy(alpha = 0.05f),
                        Color.Transparent,
                    ),
                    radius = 700f,
                )
            )
            .padding(top = 14.dp, bottom = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Watermark globe
        Text(
            "🌍",
            fontSize = 240.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-30).dp)
                .alpha(0.06f),
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    playerTotal.toString(),
                    color = Color.White,
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "игроков",
                    color = LbTextDim,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "В $countriesTotal ${pluralCountries(countriesTotal)}".uppercase(),
                color = LbTextDim,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp,
            )
        }
    }
}

@Composable
private fun WorldSegmentSwitch(segment: WorldSegment, onChange: (WorldSegment) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(LbSurface2)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        SegmentItem(
            label = "🏆 Страны",
            selected = segment == WorldSegment.COUNTRIES,
            onClick = { onChange(WorldSegment.COUNTRIES) },
            modifier = Modifier.weight(1f),
        )
        SegmentItem(
            label = "👤 Игроки",
            selected = segment == WorldSegment.PLAYERS,
            onClick = { onChange(WorldSegment.PLAYERS) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SegmentItem(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) LbSurface3 else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (selected) LbText else LbTextMute,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun CountryAggregateRow(
    rank: Int,
    country: CountryAggregate,
    isMyCountry: Boolean,
) {
    val isTop3 = rank <= 3
    val totalXpFormatted = formatCompact(country.totalXp)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (isMyCountry) Color.Transparent else LbSurface,
    ) {
        Row(
            modifier = Modifier
                .then(
                    if (isMyCountry) Modifier
                        .background(
                            Brush.horizontalGradient(
                                listOf(LbPrimary.copy(alpha = 0.18f), LbPrimary.copy(alpha = 0.04f))
                            ),
                            shape = RoundedCornerShape(12.dp),
                        )
                        .border(1.5.dp, LbPrimary, RoundedCornerShape(12.dp))
                    else Modifier
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "#$rank",
                color = if (isTop3) LbGold else LbTextDim,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.width(28.dp),
            )
            Text(CountryNames.flagOf(country.iso), fontSize = 22.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (isMyCountry) "${CountryNames.nameOf(country.iso)} (ты тут)"
                    else CountryNames.nameOf(country.iso),
                    color = LbText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "${country.playerCount} ${pluralPlayers(country.playerCount)} · ср. ${country.avgXp} XP",
                    color = LbTextMute,
                    fontSize = 10.sp,
                )
            }
            Text(
                totalXpFormatted,
                color = LbPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}

@Composable
private fun MyRankPill(rank: Int, total: Int, where: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = LbPrimary.copy(alpha = 0.12f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("⭐", fontSize = 18.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Ты #$rank из $total",
                        color = LbPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "в $where",
                        color = LbTextDim,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(text: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = LbTextDim, fontSize = 13.sp)
    }
}

// ═══════════════════════════════════════════════════════════
//  OPT-IN (вход в лидерборд)
// ═══════════════════════════════════════════════════════════
@Composable
private fun OptInBlock(
    displayName: String,
    onJoin: () -> Unit,
    onChangeName: (String) -> Unit
) {
    var name by remember { mutableStateOf(if (displayName == "Estudiante") "" else displayName) }
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
            fontWeight = FontWeight.ExtraBold,
            color = LbText,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(R.string.lb_join_explain),
            fontSize = 14.sp,
            color = LbTextDim,
        )
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it.take(20) },
            label = { Text(stringResource(R.string.lb_nickname)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = LbText,
                unfocusedTextColor = LbText,
                focusedBorderColor = LbPrimary,
                unfocusedBorderColor = LbLine,
                focusedLabelColor = LbPrimary,
                unfocusedLabelColor = LbTextDim,
            ),
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                if (name.isNotBlank() && name != displayName) onChangeName(name)
                onJoin()
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = name.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = LbPrimary),
        ) {
            Text(stringResource(R.string.lb_join_button), fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  DIALOGS (name prompt + country picker)
// ═══════════════════════════════════════════════════════════
@Composable
private fun NamePromptDialog(
    initialName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember(initialName) {
        mutableStateOf(if (initialName == "Estudiante") "" else initialName)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Как тебя называть в рейтинге?") },
        text = {
            Column {
                Text(
                    "Выбери уникальный никнейм — другие игроки увидят его в топе. " +
                    "Можно изменить позже в настройках профиля.",
                    fontSize = 13.sp,
                    color = LbTextDim,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(20) },
                    label = { Text("Никнейм") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.trim().isNotBlank()) onConfirm(name.trim()) },
                enabled = name.trim().isNotBlank() && name.trim() != "Estudiante",
                colors = ButtonDefaults.buttonColors(containerColor = LbPrimary),
            ) {
                Text("Присоединиться", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

@Composable
private fun CountryPickerDialog(
    currentIso: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val countries = remember { CountryNames.allCountries() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выбери страну") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
            ) {
                itemsIndexed(countries) { _, country ->
                    val isSelected = country.iso == currentIso
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(country.iso) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(country.flag, fontSize = 22.sp)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            country.name,
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) LbPrimary else LbText,
                            modifier = Modifier.weight(1f),
                        )
                        if (isSelected) Text("✓", fontSize = 18.sp, color = LbPrimary)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } }
    )
}

// ═══════════════════════════════════════════════════════════
//  Helpers
// ═══════════════════════════════════════════════════════════
private fun LeaderboardEntry.toPodium(): PodiumEntry = PodiumEntry(
    name = nickname,
    rating = skillRating,
    uid = uid,
    flag = CountryNames.flagOf(country),
)

private fun formatCompact(n: Long): String = when {
    n >= 1_000_000_000 -> "${n / 1_000_000_000}B"
    n >= 1_000_000 -> "${n / 1_000_000}M"
    n >= 1_000 -> "${n / 1_000}K"
    else -> n.toString()
}

private fun pluralPlayers(n: Int): String = when {
    n % 10 == 1 && n % 100 != 11 -> "игрок"
    n % 10 in 2..4 && (n % 100 < 10 || n % 100 >= 20) -> "игрока"
    else -> "игроков"
}

private fun pluralCountries(n: Int): String = when {
    n % 10 == 1 && n % 100 != 11 -> "стране"
    else -> "странах"
}
