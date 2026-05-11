package com.spanishapp.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.content.ContentVersionStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Tells [SpanishNavHost] whether the initial content download is done.
 *
 * `ready == null` while we're still reading DataStore — show splash.
 * `ready == false` → force DownloadScreen as the start destination.
 * `ready == true`  → proceed with normal auth/onboarding routing.
 */
@HiltViewModel
class ContentReadyGate @Inject constructor(
    versionStore: ContentVersionStore,
) : ViewModel() {

    val ready: StateFlow<Boolean?> = versionStore.contentReady
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
