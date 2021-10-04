package com.example.quizapp.model.ktor

sealed class ResourceStatus<out T>(val data: T?, val throwable: Throwable?) {
    class Success<T>(data : T? = null) : ResourceStatus<T>(data, null)
    class Error<T>(throwable : Throwable? = null, data : T? = null) : ResourceStatus<T>(data, throwable)
    class Loading<T>(data: T? = null) : ResourceStatus<T>( data, null)
}