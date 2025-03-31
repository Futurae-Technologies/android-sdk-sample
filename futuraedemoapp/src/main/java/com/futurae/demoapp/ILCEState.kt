package com.futurae.demoapp

sealed interface ILCEState<out T> {
    data object Idle : ILCEState<Nothing>
    data object Loading : ILCEState<Nothing>
    data class Content<T>(val data: T) : ILCEState<T>
    data class Error(val throwable: Throwable) : ILCEState<Nothing>
}