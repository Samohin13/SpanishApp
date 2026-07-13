package com.spanishapp.ui.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.LibroProgressDao
import com.spanishapp.domain.games.GameId
import com.spanishapp.domain.games.GameLevelManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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

    // v1.26.1 FIX: РЕАКТИВНЫЙ прогресс через Flow. Раньше refresh() звался
    // только в init (один раз) — вернувшись в хаб после прохождения уровня,
    // юзер видел старое «0/100», т.к. ViewModel переиспользовался и данные не
    // перечитывались. Теперь observeOverview эмитит при каждом изменении в
    // Room → обложка обновляется сама.
    private val gameFlows: List<kotlinx.coroutines.flow.Flow<Pair<String, GameProgressInfo>?>> =
        routeToGameId.map { (route, gameId) ->
            levelManager.observeOverview(gameId).map<_, Pair<String, GameProgressInfo>?> { ov ->
                route to GameProgressInfo("${ov.nextLevel - 1} / 100")
            }
        } + libroDao.getAll().map<_, Pair<String, GameProgressInfo>?> { libros ->
            val n = libros.count { it.isCompleted }
            if (n > 0) "game_libros" to GameProgressInfo("$n прочитано") else null
        }

    val gameProgress: StateFlow<Map<String, GameProgressInfo>> =
        combine(gameFlows) { arr -> arr.filterNotNull().toMap() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
}
