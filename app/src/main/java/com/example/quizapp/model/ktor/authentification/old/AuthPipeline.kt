package com.example.quizapp.model.ktor.authentification.old

import com.example.quizapp.extensions.log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.date.*
import io.ktor.utils.io.*
import okhttp3.Credentials
import okhttp3.internal.addHeaderLenient
import kotlin.coroutines.CoroutineContext

//TODO -> Find out how this works for ktor
class AuthPipeline(httpClient: HttpClient) {

    init {
        httpClient.receivePipeline.intercept(HttpReceivePipeline.State) {
//            if(response.request.url.encodedPath in OkHttpBasicAuthInterceptor.URLS_TO_IGNORE){
//                proceed()
//                return@intercept
//            }
//            response.headers.apply {
//                addHeaderLenient()
//            }

//            context.response.headers.
        }



//        httpClient.receivePipeline.intercept(HttpReceivePipeline.Before) { response ->
//            if(response.request.url.encodedPath in OkHttpBasicAuthInterceptor.URLS_TO_IGNORE){
//                proceed()
//                return@intercept
//            }
//
//            val authResponse = object : HttpResponse() {
//                override val call: HttpClientCall = response.call
//                override val content: ByteReadChannel = response.content
//                override val coroutineContext: CoroutineContext = response.coroutineContext
//                override val headers: Headers = HeadersBuilder().apply {
//                    appendAll(response.headers)
//                    append(HttpHeaders.Authorization, Credentials.basic("email", "password"))
//                    this.remove(HttpHeaders.CacheControl)
//                    this.append(HttpHeaders.CacheControl, "public, max-age=31536000")
//                }.build()
//                override val requestTime: GMTDate = response.requestTime
//                override val responseTime: GMTDate = response.responseTime
//                override val status: HttpStatusCode = response.status
//                override val version: HttpProtocolVersion = response.version
//            }
//
//            log("${authResponse.headers.contains(HttpHeaders.Authorization)}")
//
//            proceedWith(authResponse)
//        }
    }
}