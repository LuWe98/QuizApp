package com.example.quizapp.model.ktor

sealed class ResourceStatus<T> {
    data class Success<T>(val message : String?, val data : T?) : ResourceStatus<T>()
    data class Error<T>(val message : String?, val data : T?) : ResourceStatus<T>()
    data class Loading<T>(val message : String?) : ResourceStatus<T>()
    object None : ResourceStatus<Nothing>()
}
