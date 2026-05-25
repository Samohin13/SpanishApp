package com.spanishapp.ui.theory

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.TheoryProgressDao
import com.spanishapp.data.theory.TheoryContent
import com.spanishapp.data.theory.TheoryContentData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для экрана чтения теории-карточки.
 *
 * Поток:
 *   1. Получаем lessonId из nav-args
 *   2. Загружаем TheoryContent из TheoryContentData
 *   3. При прокрутке до конца / тапе «Прочитал» — markRead
 */
@HiltViewModel
class TheoryReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val theoryProgressDao: TheoryProgressDao,
    private val hintBank: com.spanishapp.service.HintBankManager,
    private val xpTracker: com.spanishapp.service.XpTracker,
    private val uiSound: com.spanishapp.service.UiSoundPlayer,
) : ViewModel() {

    val lessonId: String = savedStateHandle.get<String>("lessonId") ?: ""

    private val _state = MutableStateFlow(TheoryReaderState())
    val state: StateFlow<TheoryReaderState> = _state.asStateFlow()

    init {
        loadTheory()
    }

    private fun loadTheory() {
        val content = TheoryContentData.byLessonId(lessonId)
        if (content == null) {
            _state.value = TheoryReaderState(notFound = true, lessonId = lessonId)
            return
        }
        viewModelScope.launch {
            val progress = theoryProgressDao.getOne(lessonId)
            _state.value = TheoryReaderState(
                content = content,
                isAlreadyRead = progress != null && progress.firstReadAt > 0,
                lastReadAt = progress?.lastReadAt ?: 0L,
                readCount = progress?.readCount ?: 0,
                lessonId = lessonId,
            )
        }
    }

    /**
     * Помечает теорию как прочитанную. Вызывается при:
     *   • Тапе на «Я понял» в конце карточки
     *   • (опц.) автоматическом scroll до конца
     */
    fun markRead() {
        viewModelScope.launch {
            // v1.16.0: +1 💡 только при ПЕРВОМ прочтении (не повторных)
            val wasFirstTime = !_state.value.isAlreadyRead
            theoryProgressDao.markRead(lessonId)
            _state.value = _state.value.copy(
                isAlreadyRead = true,
                readCount = _state.value.readCount + 1,
                justMarkedRead = true,
            )
            if (wasFirstTime) {
                hintBank.award(1, com.spanishapp.service.HintEarnReason.THEORY_READ)
                // v1.22.16: XP за первое прочтение теория-карточки.
                xpTracker.add(
                    xp = com.spanishapp.domain.algorithm.XpSystem.THEORY_FIRST_READ,
                    words = 0,
                )
                // SFX: page-flip звук при первом «понял» — даёт юзеру
                // тактильную обратную связь что страница «закрыта».
                uiSound.play(com.spanishapp.service.UiSoundPlayer.Sound.PAGE)
            }
        }
    }
}

data class TheoryReaderState(
    val content: TheoryContent? = null,
    val isAlreadyRead: Boolean = false,
    val lastReadAt: Long = 0L,
    val readCount: Int = 0,
    val notFound: Boolean = false,
    val lessonId: String = "",
    val justMarkedRead: Boolean = false,
)
