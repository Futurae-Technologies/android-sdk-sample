package com.futurae.sampleapp.ui.shared.elements.resultinformativescreen

import com.futurae.sampleapp.ui.TextWrapper

data class ResultInformativeScreenUIState(
    val state: ResultState,
    val title: TextWrapper,
    val actionCta: TextWrapper?
)

enum class ResultState {
    LOADING,
    SUCCESS,
    ERROR
}