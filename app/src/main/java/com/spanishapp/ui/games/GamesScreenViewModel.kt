package com.spanishapp.ui.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.LibroProgressDao
import com.spanishapp.domain.games.GameId
import com.spanishapp.domain.games.GameLevelManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Единая модель прогресса для тайла игры. Формат подписи везде:
 *   «N / 100»          — сколько уровней пройдено хотя бы на 1 звезду
 *   «X прочитано»      — для Libros (свой формат)
 */
data class GameProgressInfo(
    val label: String
)

@HiltViewModel
class GamesScreenViewModel @Inject constructor(
    private val libroDao: LibroProgressDao,
    private val levelManager: GameLevelManager
) : ViewModel() {

    /** route → gameId mapping. Только level-based игры (100 уровней).
     *  Verbos (тренажёр глаголов) НЕ попадает сюда — у него tier-based
     *  система (top-50/100/200/350/full), не уровни. Поэтому «0/100» на
     *  его тайле было бессмысленным и убрано. */
    private val routeToGameId = mapOf(
        "game_articles"  to GameId.ARTICLES,
        "game_speed"     to GameId.SPEED,
        "game_sopa"      to GameId.SOPA,
        "game_palabra"   to GameId.PALABRA,
        "game_math"      to GameId.MATH,
        "game_crossword" to GameId.CROSSWORD
    )

    private val _gameProgress = MutableStateFlow<Map<String, GameProgressInfo>>(emptyMap())
    val gameProgress: StateFlow<Map<String, GameProgressInfo>> = _gameProgress.asStateFlow()

    // init MUST come after routeToGameId — иначе refresh() стартует когда
    // map ещё null и падает с NPE на entries (вылет произошёл именно тут).
    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            val map = HashMap<String, GameProgressInfo>()

            // ── Универсально: для всех level-based игр — реальные данные.
            // Показываем подпись ВСЕГДА (даже 0/100) — это даёт юзеру понять
            // что у игры есть 100 уровней и сколько он прошёл. Раньше при 0
            // подпись скрывалась полностью.
            for ((route, gameId) in routeToGameId) {
                val cleared = levelManager.nextLevel(gameId) - 1
                map[route] = GameProgressInfo("$cleared / 100")
            }

            // ── Libros: пройдено рассказов (свой формат, не /100) ──
            val libros = libroDao.getAll().first()
            val libCompleted = libros.count { it.isCompleted }
            if (libCompleted > 0) {
                map["game_libros"] = GameProgressInfo("$libCompleted прочитано")
            }

            _gameProgress.value = map
        }
    }
}
