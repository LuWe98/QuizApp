package com.example.quizapp.model.ktor.status

sealed class Resource<out T>(val data: T?, val throwable: Throwable?) {
    class Success<T>(data : T? = null) : Resource<T>(data, null)
    class Error<T>(throwable : Throwable? = null, data : T? = null) : Resource<T>(data, throwable)
    class Loading<T>(data: T? = null) : Resource<T>( data, null)
}