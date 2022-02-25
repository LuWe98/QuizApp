package com.example.quizapp.model.ktor.client

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.Claim
import com.example.quizapp.extensions.log
import com.example.quizapp.model.datastore.PreferenceRepository
import com.example.quizapp.model.ktor.apiclasses.UserApiImpl
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.auth.providers.*
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class KtorClientAuth @Inject constructor(
    private val preferenceRepository: PreferenceRepository,
    private val ktorClientProvider: Provider<HttpClient>,
    private val userApiProvider: Provider<UserApiImpl>
) {

    companion object {
        private const val EMPTY_TOKEN = ""
        const val CLAIM_USER_ROLE = "userRoleClaim"
        const val CLAIM_CAN_SHARE_QUESTIONNAIRE_WITH = "canShareQuestionnaireWithClaim"
    }

    private val client get() = ktorClientProvider.get()
    private val clientAuth get() = client.feature(Auth)
    private val userApi get() = userApiProvider.get()

    fun registerJwtAuth(auth: Auth) = auth.bearer {
        loadTokens {
            (preferenceRepository.getJwtToken() ?: EMPTY_TOKEN).let { token ->
                BearerTokens(token, token)
            }
        }

        refreshTokens {
            runCatching {
                userApi.refreshJwtToken(
                    preferenceRepository.getUserName(),
                    preferenceRepository.getUserPassword()
                )
            }.onSuccess { response ->
                log("NEW TOKEN: ${response.token}")
                preferenceRepository.updateJwtToken(response.token)
                return@refreshTokens BearerTokens(response.token, response.token)
            }.onFailure {
                preferenceRepository.updateJwtToken(EMPTY_TOKEN)
            }
            BearerTokens(EMPTY_TOKEN, EMPTY_TOKEN)
        }
    }

    fun resetJwtAuth() {
        clientAuth?.let { auth ->
            auth.providers.clear()
            registerJwtAuth(auth)
        }
    }
}

fun String.claimAsBoolean(claimKey: String): Boolean = getClaimFromToken(claimKey).asBoolean()

fun String.claimAsString(claimKey: String): String = getClaimFromToken(claimKey).asString()

fun String.getClaimFromToken(claimKey: String) : Claim = JWT.decode(this).getClaim(claimKey)