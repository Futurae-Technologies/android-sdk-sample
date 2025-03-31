package com.futurae.demoapp.ui.shared.elements.resultinformativescreen

import com.futurae.demoapp.ui.TextWrapper

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