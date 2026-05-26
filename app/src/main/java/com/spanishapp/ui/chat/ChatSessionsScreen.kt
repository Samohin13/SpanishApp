package com.spanishapp.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

/**
 * Picker screen for chat themes (Travel / Restaurant / Interview / etc.).
 * Tapping a theme opens [AiChatScreen] with the theme's session id, which
 * keeps history per theme and rewires the system prompt.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSessionsScreen(navController: NavHostController) {
    // v1.23.6: тематические чаты (Путешествие, Ресторан, и т.д.) = PRO.
    // Только «Свободный чат» (id=default) бесплатен с лимитом 50/день.
    val isPro by com.spanishapp.ui.games.common.rememberIsProState()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "AI-помощник",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    "Выбери тему — это даст AI-помощнику нужный контекст для практики",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(ChatSessions.all, key = { it.id }) { theme ->
                val isProLocked = !isPro && theme.id != "default"
                ThemeCard(
                    theme = theme,
                    isProLocked = isProLocked,
                    onClick = {
                        if (isProLocked) {
                            navController.navigate("paywall") { launchSingleTop = true }
                        } else {
                            navController.navigate("ai_chat?sessionId=${theme.id}")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ThemeCard(
    theme: ChatSessionTheme,
    isProLocked: Boolean = false,
    onClick: () -> Unit
) {
    val proPrimary = Color(0xFFFF8A3D)
    com.spanishapp.ui.components.PressableCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        backgroundColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (isProLocked) proPrimary.copy(alpha = 0.18f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(if (isProLocked) "💎" else theme.emoji, fontSize = 28.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        theme.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (isProLocked) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(99.dp))
                                .background(proPrimary)
                                .padding(horizontal = 7.dp, vertical = 1.dp)
                        ) {
                            Text("PRO", color = Color.White, fontSize = 9.sp,
                                fontWeight = FontWeight.Black)
                        }
                    }
                }
                Text(
                    if (isProLocked) "Открой с PRO →" else theme.subtitle,
                    fontSize = 13.sp,
                    color = if (isProLocked) proPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
