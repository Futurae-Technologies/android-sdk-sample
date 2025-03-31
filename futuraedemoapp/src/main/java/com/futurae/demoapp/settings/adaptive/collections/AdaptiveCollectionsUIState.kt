package com.futurae.demoapp.settings.adaptive.collections

import androidx.annotation.StringRes

sealed class AdaptiveCollectionsUIState {
    data object Idle: AdaptiveCollectionsUIState()
    data class BlankSlate(@StringRes val message: Int): AdaptiveCollectionsUIState()
    data class Collections(val collections: List<AdaptiveCollectionUIState>): AdaptiveCollectionsUIState()
}

data class AdaptiveCollectionUIState(
    val formattedTimestamp: String,
    val json: String
)