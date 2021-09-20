package com.example.quizapp.model.ktor.apiclasses

import com.example.quizapp.model.Todo
import com.example.quizapp.model.ktor.requests.GetTodoRequest
import com.example.quizapp.utils.Constants
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoCalls @Inject constructor(
    private val ktorClient: HttpClient
) {

    suspend fun getTodos(): List<Todo> = ktorClient.get("${Constants.EXTERNAL_DATABASE_URL}/todos")

    suspend fun getTodo(todoId: Int): Todo = ktorClient.post("${Constants.EXTERNAL_DATABASE_URL}/todo") {
        contentType(ContentType.Application.Json)
        body = GetTodoRequest(todoId.toString())
    }
}