package com.example.quizapp.model.ktor.exceptions

import com.example.quizapp.extensions.log
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.room.LocalRepository
import io.ktor.client.statement.*
import io.ktor.http.*
import javax.inject.Inject
import javax.inject.Singleton


//TODO -> USER MUSS DANN AUSGELOGGT WERDEN!

@Singleton
class BackendExceptionHandler @Inject constructor(
    private val localRepository: LocalRepository,
    private val preferencesRepository: PreferencesRepository
) {

    fun validateResponse(response: HttpResponse){
        when(response.status){
            HttpStatusCode.Unauthorized -> throw UnauthorizedException()
        }
    }

    fun handleException(throwable: Throwable){
        when(throwable){
            is UnauthorizedException -> {
                log("Has To Logout")
            }
        }
    }
}