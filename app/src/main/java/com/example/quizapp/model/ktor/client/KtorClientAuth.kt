package com.example.quizapp.model.ktor.client

import com.auth0.jwt.JWT
import com.example.quizapp.extensions.log
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.apiclasses.UserApi
import com.example.quizapp.model.databases.properties.Role
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.auth.providers.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class KtorClientAuth @Inject constructor(
    private val applicationScope: CoroutineScope,
    private val preferencesRepository: PreferencesRepository,
    private val ktorClientProvider: Provider<HttpClient>,
    private val userApiProvider: Provider<UserApi>
) {

    companion object {
        const val EMPTY_TOKEN = ""
        const val CLAIM_USER_ROLE = "userRole"
    }

    private val client get() = ktorClientProvider.get()
    private val clientAuth get() = client.feature(Auth)
    private val userApi get() = userApiProvider.get()
    private val cachedUser get() = preferencesRepository.user

    fun registerJwtAuth(auth: Auth) {
        auth.bearer {
            loadTokens {
                (preferencesRepository.getJwtToken() ?: EMPTY_TOKEN).let { token ->
                    BearerTokens(token, token)
                }
            }

            refreshTokens {
                runCatching {
                    userApi.refreshJwtToken(cachedUser.userName, cachedUser.password)
                }.onSuccess { response ->
                    log("NEW TOKEN: ${response.token}")
                    updateUserRole(response.token)

                    preferencesRepository.updateJwtToken(response.token)

                    (response.token ?: EMPTY_TOKEN).let { token ->
                        return@refreshTokens BearerTokens(token, token)
                    }
                }.onFailure {
                    preferencesRepository.updateJwtToken(EMPTY_TOKEN)
                }

                BearerTokens(EMPTY_TOKEN, EMPTY_TOKEN)
            }
        }
    }

    fun resetJwtAuth() {
        clientAuth?.let { auth ->
            auth.providers.clear()
            registerJwtAuth(auth)
        }
    }

    private fun updateUserRole(token: String?) {
        if (token == null) return

        applicationScope.launch(IO) {
            JWT.decode(token).getClaim(CLAIM_USER_ROLE).asString().let { roleString ->
                if(roleString.isNotBlank()) {
                    runCatching {
                        Role.valueOf(roleString)
                    }.onSuccess { role ->
                        preferencesRepository.updateUserRole(role)
                    }
                }
            }
        }
    }
}