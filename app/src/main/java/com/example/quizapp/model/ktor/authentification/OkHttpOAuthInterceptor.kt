package com.example.quizapp.model.ktor.authentification

import com.example.quizapp.model.datastore.PreferencesRepository
import io.ktor.http.*
import okhttp3.Credentials
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OkHttpOAuthInterceptor @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        chain.run {
            if (shouldIgnoreUrl(request().url)) {
                return proceed(request())
            }

            //TODO -> Change Logic to OAuth instead of basic
            return proceed(request().newBuilder().run {
                preferencesRepository.getUserCredentials().let { credentials ->
                    header(HttpHeaders.Authorization, Credentials.basic(credentials.name, credentials.password))
                    build()
                }
            })
        }
    }

    private fun shouldIgnoreUrl(url: HttpUrl) = url.encodedPath in URLS_TO_IGNORE

    companion object {
        val URLS_TO_IGNORE = listOf(
            "register",
            "/register",
            "login",
            "/login"
        )
    }
}