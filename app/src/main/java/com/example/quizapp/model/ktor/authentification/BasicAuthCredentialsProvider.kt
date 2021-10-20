package com.example.quizapp.model.ktor.authentification

import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.utils.Constants
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.auth.providers.*
import io.ktor.http.*
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class BasicAuthCredentialsProvider @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) {

    fun registerBasicAuth(auth: Auth) {
        auth.basic {
            realm = Constants.REALM

            credentials { preferencesRepository.user.asBasicAuthCredentials }

            sendWithoutRequest { request ->
                shouldSendWithoutRequest(request.url).also { shouldSendWithoutRequest ->
                    if (shouldSendWithoutRequest) {
                        auth.providers.clear()
                        registerBasicAuth(auth)
                    }
                }
            }
        }
    }

    private fun shouldSendWithoutRequest(url: URLBuilder) = url.encodedPath !in URLS_TO_IGNORE

    companion object {
        private val URLS_TO_IGNORE = listOf(
            "user/register",
            "user/login"
        )
    }


    //                        ktorClient.get().feature(Auth)?.let { auth ->
//                            auth.providers.clear()
//                            registerBasicAuth(auth)
//                        }

}