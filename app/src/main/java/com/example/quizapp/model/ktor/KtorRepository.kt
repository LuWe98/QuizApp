package com.example.quizapp.model.ktor

import com.example.quizapp.model.ktor.apiclasses.TodoCalls
import com.example.quizapp.model.Todo
import com.example.quizapp.model.ktor.apiclasses.UserCalls
import com.example.quizapp.model.ktor.responses.BasicResponse
import io.ktor.client.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KtorRepository @Inject constructor(
    private val todoCalls: TodoCalls,
    private val userCalls : UserCalls
) {

    // TO DO CALLS
    fun getTodos(): Flow<List<Todo>> = flow { emit(todoCalls.getTodos()) }.flowOn(Dispatchers.IO)

    suspend fun getTodo(todoId : Int) = todoCalls.getTodo(todoId)



    // USER CALLS
    suspend fun loginUser(email : String, password : String) = userCalls.loginUser(email, password)

    suspend fun registerUser(email: String, username: String, password: String) = userCalls.registerUser(email, username, password)
}