package com.example.quizapp.model.ktor

import io.ktor.http.*
import okhttp3.Credentials
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Singleton

@Singleton
class OkHttpAuthInterceptor : Interceptor {

    val email: String = "Hallo@gmx.de"
    val password: String = "1234"

    override fun intercept(chain: Interceptor.Chain): Response {
        chain.run {
            if (shouldIgnoreUrl(request().url)) {
                return proceed(request())
            }

            return proceed(request().newBuilder().run {
                header(HttpHeaders.Authorization, Credentials.basic(email, password))
                build()
            })
        }
    }

    private fun shouldIgnoreUrl(url : HttpUrl) = url.encodedPath in URLS_TO_IGNORE

    companion object {
        val URLS_TO_IGNORE = listOf(
            "register",
            "/register",
            "login",
            "/login"
        )
    }
}