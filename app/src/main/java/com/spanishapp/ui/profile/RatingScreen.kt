package com.spanishapp.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.spanishapp.R
import com.spanishapp.domain.algorithm.LeagueResolver
import com.spanishapp.ui.components.LeagueBadge
import com.spanishapp.ui.components.SpanishFlagRating

private enum class SortMode(val labelRes: Int) {
    WEAK(R.string.rating_sort_weak), STRONG(R.string.rating_sort_strong), ALPHABET(R.string.rating_sort_alpha)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingScreen(
    navController: NavHostController,
    vm: ProfileViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val items by vm.categoryRatings.collectAsState()
    var sortMode by remember { mutableStateOf(SortMode.WEAK) }
    val league = LeagueResolver.fromTier(state.progress.currentLeague.coerceAtLeast(1))

    // Refresh when entering screen
    LaunchedEffect(Unit) { vm.refreshCategoryRatings() }

    val context = LocalContext.current
    val sorted = remember(items, sortMode, context) {
        when (sortMode) {
            SortMode.WEAK -> items.sortedBy { it.score }
            SortMode.STRONG -> items.sortedByDescending { it.score }
            SortMode.ALPHABET -> items.sortedBy { context.getString(it.labelRes) }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                title = { Text(androidx.compose.ui.res.stringResource(R.string.rating_topic_progress), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            // Шапка — текущая лига
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(androidx.compose.ui.res.stringResource(R.string.rating_now), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(8.dp))
                    LeagueBadge(league = league)
                    Spacer(Modifier.weight(1f))
                    Text("${state.progress.skillRating}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Сортировка
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                SortMode.values().forEach { mode ->
                    val selected = sortMode == mode
                    AssistChip(
                        onClick = { sortMode = mode },
                        label = { Text(androidx.compose.ui.res.stringResource(mode.labelRes), fontSize = 12.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (sorted.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        androidx.compose.ui.res.stringResource(R.string.rating_empty),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                    items(sorted, key = { it.key }) { item ->
                        CategoryRow(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryRow(item: CategoryRatingUi) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.icon != null) {
                Box(
                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(item.icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(androidx.compose.ui.res.stringResource(item.labelRes), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    "${item.learned}/${item.total} • ${(item.score * 100).toInt()}%",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            SpanishFlagRating(filled = item.flags, flagWidthDp = 16, flagHeightDp = 11)
        }
    }
}
