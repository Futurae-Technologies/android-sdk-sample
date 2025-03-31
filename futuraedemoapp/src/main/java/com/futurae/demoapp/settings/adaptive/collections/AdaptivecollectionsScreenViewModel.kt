package com.futurae.demoapp.settings.adaptive.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futurae.demoapp.R
import com.futurae.sdk.FuturaeSDK
import com.futurae.sdk.adaptive.AdaptiveDbHelper
import com.futurae.sdk.adaptive.model.AdaptiveCollection
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class AdaptiveCollectionsScreenViewModel : ViewModel() {

    private val gson = GsonBuilder().setLenient().create()
    private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    private val _state = MutableStateFlow<AdaptiveCollectionsUIState>(AdaptiveCollectionsUIState.Idle)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            AdaptiveDbHelper.getAllCollections().collect {
                _state.emit(constructState(it))
            }
        }
    }

    fun clearCollections() {
        AdaptiveDbHelper.deleteAllCollections()
    }

    private fun constructState(collections: List<AdaptiveCollection>) = when {
        collections.isEmpty() && FuturaeSDK.client.accountApi.getActiveAccounts().isEmpty() -> {
            AdaptiveCollectionsUIState.BlankSlate(message = R.string.adaptive_enroll_to_collect)
        }

        collections.isEmpty() -> {
            AdaptiveCollectionsUIState.BlankSlate(message = R.string.adaptive_no_collections_yet)
        }

        else -> {
            val sortedItems = collections
                .sortedBy { coll -> coll.timestamp }
                .map {
                    AdaptiveCollectionUIState(
                        formattedTimestamp = simpleDateFormat.format(it.timestamp * 1000),
                        json = gson.toJson(it)
                    )
                }
            AdaptiveCollectionsUIState.Collections(sortedItems)
        }
    }
}