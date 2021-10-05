package com.example.quizapp.model.ktor

import com.example.quizapp.model.ktor.apiclasses.FilledQuestionnaireApi
import com.example.quizapp.model.ktor.apiclasses.QuestionnaireApi
import com.example.quizapp.model.ktor.apiclasses.UserApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackendRepository @Inject constructor(
    private val userApi : UserApi,
    private val questionnaireApi: QuestionnaireApi,
    private val filledQuestionnaireApi: FilledQuestionnaireApi
) {

    // USER
    suspend fun loginUser(userName : String, password : String) = userApi.loginUser(userName, password)

    suspend fun registerUser(userName: String, password: String, courseOfStudies : String) = userApi.registerUser(userName, password, courseOfStudies)

    suspend fun updateUser(newUserName : String) = userApi.updateUser(newUserName)

    suspend fun deleteUser() = userApi.deleteUser()



    // QUESTIONNAIRES
    suspend fun getQuestionnairesOfUser() = questionnaireApi.getQuestionnaireOfUser()

    suspend fun getAllQuestionnaires() = questionnaireApi.getAllQuestionnaires()

}