package com.example.quizapp.model.ktor.client

import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.client.KtorClientExceptionHandler.UserCredentialsErrorType.*
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.statement.*
import io.ktor.http.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KtorClientExceptionHandler @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) {

    suspend fun validateResponse(response: HttpResponse) {
        when (response.status) {
            HttpStatusCode.BadRequest -> {
                runCatching {
                    response.receive<UserCredentialsErrorType>()
                }.onSuccess { error ->
                    when (error) {
                        CREDENTIALS_CHANGED -> throw UserCredentialsChangedException()
                        USER_DOES_NOT_EXIST -> throw UserDoesNotExistException()
                    }
                }.onFailure {
                    throw ResponseException(response, "Request was bad!")
                }
            }
        }
    }

    suspend fun handleException(throwable: Throwable) {
        when(throwable){
            is UserCredentialsChangedException, is UserDoesNotExistException -> {
                preferencesRepository.clearPreferenceData()
            }
        }
    }

    class UserCredentialsChangedException : IllegalStateException()

    class UserDoesNotExistException : IllegalStateException()

    enum class UserCredentialsErrorType {
        CREDENTIALS_CHANGED,
        USER_DOES_NOT_EXIST
    }
}