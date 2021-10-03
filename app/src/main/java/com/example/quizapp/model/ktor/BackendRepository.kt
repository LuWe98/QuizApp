package com.example.quizapp.model.ktor

import com.example.quizapp.model.Todo
import com.example.quizapp.model.ktor.apiclasses.QuestionnaireApi
import com.example.quizapp.model.ktor.apiclasses.TodoCalls
import com.example.quizapp.model.ktor.apiclasses.UserApi
import io.ktor.client.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackendRepository @Inject constructor(
    private val todoCalls: TodoCalls,
    private val userApi : UserApi,
    private val questionnaireApi: QuestionnaireApi
) {

    // TO DO CALLS
    fun getTodos(): Flow<List<Todo>> = flow { emit(todoCalls.getTodos()) }.flowOn(Dispatchers.IO)

    suspend fun getTodo(todoId : Int) = todoCalls.getTodo(todoId)



    // USER CALLS
    suspend fun loginUser(userName : String, password : String) = userApi.loginUser(userName, password)

    suspend fun registerUser(userName: String, password: String, courseOfStudies : String) = userApi.registerUser(userName, password, courseOfStudies)

    suspend fun updateUser(newUserName : String) = userApi.updateUser(newUserName)



    // QUESTIONNAIRES
    suspend fun getQuestionnairesOfUser() = questionnaireApi.getQuestionnaireOfUser()

}