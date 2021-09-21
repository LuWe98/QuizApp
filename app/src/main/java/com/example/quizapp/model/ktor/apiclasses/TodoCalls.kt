package com.example.quizapp.model.ktor.apiclasses

import com.example.quizapp.model.Todo
import com.example.quizapp.model.ktor.requests.QuestionnairesRequest
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoCalls @Inject constructor(
    private val ktorClient: HttpClient
) {

    suspend fun getTodos(): List<Todo> = ktorClient.get("/todos")

    suspend fun getTodo(todoId: Int): Todo = ktorClient.post("/todo") {
        body = QuestionnairesRequest(todoId.toString())
    }
}