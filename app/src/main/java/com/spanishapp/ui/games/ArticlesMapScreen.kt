package com.spanishapp.ui.games

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.spanishapp.data.db.dao.ArticleGameDao
import com.spanishapp.data.db.entity.ArticleLevelProgressEntity
import com.spanishapp.ui.theme.AppColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.sin

@HiltViewModel
class ArticlesMapViewModel @Inject constructor(
    private val dao: ArticleGameDao
) : ViewModel() {
    val levels: StateFlow<List<ArticleLevelProgressEntity>> = dao.getAllProgress()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticlesMapScreen(
    navController: NavHostController,
    vm: ArticlesMapViewModel = hiltViewModel()
) {
    val levels by vm.levels.collectAsState()
    val scrollState = rememberScrollState()

    // Скорллим вниз (к началу уровней) при первом запуске
    LaunchedEffect(levels) {
        if (levels.isNotEmpty()) {
            scrollState.scrollTo(scrollState.maxValue)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Путь Артиклей", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFFF4EAD5) // Песочный фон степей
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Карта
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(vertical = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val reversedLevels = levels.reversed()
                
                reversedLevels.forEachIndexed { index, level ->
                    val actualIndex = levels.size - index
                    val xOffset = (sin(actualIndex * 0.8f) * 80).dp
                    
                    LevelNode(
                        level = level,
                        offset = xOffset,
                        isLast = index == 0,
                        isFirst = index == reversedLevels.size - 1,
                        onLevelClick = {
                            if (level.isUnlocked) {
                                navController.navigate("game_articles_session/${level.levelId}")
                            }
                        }
                    )
                    
                    if (index < reversedLevels.size - 1) {
                        PathConnector(
                            currentOffset = xOffset,
                            nextOffset = (sin((actualIndex - 1) * 0.8f) * 80).dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelNode(
    level: ArticleLevelProgressEntity,
    offset: androidx.compose.ui.unit.Dp,
    isLast: Boolean,
    isFirst: Boolean,
    onLevelClick: () -> Unit
) {
    val nodeColor = if (level.isUnlocked) AppColors.Ochre else Color.Gray
    val biomeText = when {
        level.levelId <= 20 -> "Степи"
        level.levelId <= 50 -> "Деревня"
        level.levelId <= 80 -> "Городок"
        else -> "Мадрид"
    }

    Column(
        modifier = Modifier
            .offset(x = offset)
            .width(100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (level.levelId % 10 == 0 || isLast || isFirst) {
            Text(
                biomeText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8B4513),
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(64.dp)
                .shadow(if (level.isUnlocked) 8.dp else 0.dp, CircleShape)
                .background(if (level.isUnlocked) Color.White else Color(0xFFD1D1D1), CircleShape)
                .border(3.dp, nodeColor, CircleShape)
                .clickable(enabled = level.isUnlocked, onClick = onLevelClick),
            contentAlignment = Alignment.Center
        ) {
            if (level.levelId == 1 && level.stars == 0) {
                // Испанский бычок (заглушка-текст, если нет ресурса)
                Text("🐂", fontSize = 32.sp)
            } else {
                Text(level.levelId.toString(), fontWeight = FontWeight.ExtraBold, color = nodeColor, fontSize = 20.sp)
            }
        }

        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(top = 4.dp)) {
            repeat(3) { i ->
                Icon(
                    Icons.Default.Star,
                    null,
                    modifier = Modifier.size(14.dp),
                    tint = if (i < level.stars) Color(0xFFFFD700) else Color.LightGray
                )
            }
        }
    }
}

@Composable
private fun PathConnector(
    currentOffset: androidx.compose.ui.unit.Dp,
    nextOffset: androidx.compose.ui.unit.Dp
) {
    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(60.dp)) {
        val startX = size.width / 2 + currentOffset.toPx()
        val endX = size.width / 2 + nextOffset.toPx()
        
        val path = Path().apply {
            moveTo(startX, 0f)
            cubicTo(
                startX, 30.dp.toPx(),
                endX, 30.dp.toPx(),
                endX, 60.dp.toPx()
            )
        }
        
        drawPath(
            path = path,
            color = Color(0xFF8B4513).copy(alpha = 0.3f),
            style = Stroke(width = 8f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f))
        )
    }
}
