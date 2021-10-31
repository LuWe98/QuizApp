package com.example.quizapp.outdated

import com.example.quizapp.model.datastore.PreferencesRepository
import io.ktor.http.*
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OkHttpBasicAuthInterceptor @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        chain.apply {
            if (shouldIgnoreUrl(request().url)) {
                return proceed(request())
            }

            return proceed(request().newBuilder().run {
                preferencesRepository.user.asBasicCredentials.let { credentials ->
                    header(HttpHeaders.Authorization, credentials)
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