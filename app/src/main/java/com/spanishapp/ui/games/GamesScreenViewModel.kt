package com.spanishapp.ui.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.ArticleGameDao
import com.spanishapp.data.db.dao.LibroProgressDao
import com.spanishapp.data.db.dao.UserProgressDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GameProgressInfo(
    val label: String   // короткая метка на карточке, например "★ 7 / 15"
)

@HiltViewModel
class GamesScreenViewModel @Inject constructor(
    private val articleDao: ArticleGameDao,
    private val libroDao: LibroProgressDao,
    private val userProgressDao: UserProgressDao
) : ViewModel() {

    private val _gameProgress = MutableStateFlow<Map<String, GameProgressInfo>>(emptyMap())
    val gameProgress: StateFlow<Map<String, GameProgressInfo>> = _gameProgress.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            val map = HashMap<String, GameProgressInfo>()

            // ── Artículos: суммарные звёзды по 5 уровням (max 15) ──
            val articleLevels = articleDao.getAllProgress().first()
            val totalStars = articleLevels.sumOf { it.stars }
            if (totalStars > 0) {
                map["game_articles"] = GameProgressInfo("★ $totalStars / 15")
            }

            // ── Libros: пройдено рассказов ────────────────────────
            val libros = libroDao.getAll().first()
            val libCompleted = libros.count { it.isCompleted }
            if (libCompleted > 0) {
                map["game_libros"] = GameProgressInfo("$libCompleted прочитано")
            }

            // ── Crucigrama: расчётный уровень по XP ──────────────
            val user = userProgressDao.getProgressOnce()
            if (user != null) {
                val cwLevel = (user.totalXp / 50).coerceAtMost(100)
                if (cwLevel > 0) {
                    map["game_crossword"] = GameProgressInfo("$cwLevel / 100")
                }
            }

            _gameProgress.value = map
        }
    }
}
