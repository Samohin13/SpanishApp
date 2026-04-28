package com.spanishapp.ui.games

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
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
import javax.inject.Inject
import kotlin.math.cos
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

    // Скролл к бычку при запуске
    LaunchedEffect(levels) {
        if (levels.isNotEmpty()) {
            val lastUnlocked = levels.findLast { it.isUnlocked }?.levelId ?: 1
            // Примерный расчет позиции: 150dp на каждый уровень
            val scrollTarget = (levels.size - lastUnlocked) * 160
            scrollState.scrollTo(scrollTarget)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EL CAMINO", fontWeight = FontWeight.Black, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color(0xFF5D4037))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFE6D5B8).copy(alpha = 0.9f),
                    titleContentColor = Color(0xFF5D4037)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF87CEEB), Color(0xFFF4EAD5), Color(0xFFD2B48C))
                    )
                )
        ) {
            // Кастомный холст для всей карты
            val mapHeight = (levels.size * 180).dp
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .drawBehind {
                        // Рисуем текстуру "старой бумаги" программно
                        drawRect(Color.Black.copy(alpha = 0.05f))
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(mapHeight)
                        .padding(vertical = 100.dp)
                ) {
                    // 1. Отрисовка пути (дороги)
                    PathCanvas(levels)

                    // 2. Отрисовка объектов мира и уровней
                    levels.reversed().forEach { level ->
                        val yPos = (levels.size - level.levelId) * 180f
                        val xOffset = (sin(level.levelId * 0.6f) * 110).dp

                        LevelPoint(
                            level = level,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = yPos.dp, x = xOffset),
                            isCurrent = level.isUnlocked && (level.levelId == levels.findLast { it.isUnlocked }?.levelId),
                            onClick = {
                                navController.navigate("game_articles_session/${level.levelId}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PathCanvas(levels: List<ArticleLevelProgressEntity>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val path = Path()
        val points = levels.map { level ->
            val y = (levels.size - level.levelId) * 180f + 130f
            val x = size.width / 2 + (sin(level.levelId * 0.6f) * 110).dp.toPx()
            Offset(x, y.dp.toPx())
        }.reversed()

        if (points.size > 1) {
            path.moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) {
                val prev = points[i - 1]
                val curr = points[i]
                val cp1 = Offset(prev.x, (prev.y + curr.y) / 2)
                val cp2 = Offset(curr.x, (prev.y + curr.y) / 2)
                path.cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, curr.x, curr.y)
            }

            // Тень дороги
            drawPath(
                path = path,
                color = Color(0xFF5D4037).copy(alpha = 0.1f),
                style = Stroke(width = 45f, cap = StrokeCap.Round)
            )
            // Сама дорога (пунктир в стиле карт)
            drawPath(
                path = path,
                color = Color(0xFFE6D5B8),
                style = Stroke(width = 30f, cap = StrokeCap.Round)
            )
            drawPath(
                path = path,
                color = Color(0xFF5D4037).copy(alpha = 0.3f),
                style = Stroke(
                    width = 6f, 
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f),
                    cap = StrokeCap.Round
                )
            )
        }
    }
}

@Composable
private fun LevelPoint(
    level: ArticleLevelProgressEntity,
    modifier: Modifier,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    val nodeColor = if (level.isUnlocked) Color(0xFFFF9800) else Color(0xFFBDBDBD)
    val starColor = Color(0xFFFFD700)

    Column(
        modifier = modifier.width(120.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Декоративные объекты мира (деревья, камни) рядом с уровнем
        Box(contentAlignment = Alignment.Center) {
            if (isCurrent) {
                // Bull is clickable and starts the level
                Box(modifier = Modifier.clickable { onClick() }) {
                    BullSprite()
                }
            } else {
                Surface(
                    onClick = onClick,
                    enabled = level.isUnlocked,
                    shape = CircleShape,
                    color = if (level.isUnlocked) Color.White else Color(0xFFE0E0E0),
                    border = BorderStroke(3.dp, nodeColor),
                    shadowElevation = 6.dp,
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = level.levelId.toString(),
                            fontWeight = FontWeight.Black,
                            color = if (level.isUnlocked) Color(0xFF5D4037) else Color.Gray,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }

        // Звезды
        Row(
            modifier = Modifier.padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            repeat(3) { i ->
                Icon(
                    Icons.Default.Star, null,
                    modifier = Modifier.size(16.dp),
                    tint = if (i < level.stars) starColor else Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun BullSprite() {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -15f,
        animationSpec = infiniteRepeatable(tween(500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = ""
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.offset(y = bounce.dp).clickable { /* Будет обработано родителем */ }
    ) {
        // "Рисуем" бычка слоями
        Box(
            modifier = Modifier
                .size(70.dp)
                .drawBehind {
                    // Тень под бычком
                    drawOval(
                        color = Color.Black.copy(alpha = 0.2f),
                        topLeft = Offset(10f, size.height - 10f),
                        size = Size(size.width - 20f, 15f)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text("🐂", fontSize = 48.sp)
        }
        
        Surface(
            color = Color(0xFFC62828),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.offset(y = (-10).dp)
        ) {
            Text(
                "¡VAMOS!", 
                color = Color.White, 
                fontSize = 10.sp, 
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}
