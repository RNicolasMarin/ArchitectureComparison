package com.example.architecturecomparison

sealed class DataState<out T> {
    data class Success<out T>(val data: T): DataState<T>()
    object Idle: DataState<Nothing>()
    object Loading: DataState<Nothing>()
    object Error: DataState<Nothing>()
}
