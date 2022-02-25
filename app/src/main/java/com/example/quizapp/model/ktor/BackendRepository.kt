package com.example.quizapp.model.ktor

import com.example.quizapp.model.ktor.apiclasses.*

interface BackendRepository {
    val courseOfStudiesApi: CourseOfStudiesApi
    val facultyApi: FacultyApi
    val questionnaireApi: QuestionnaireApi
    val filledQuestionnaireApi: FilledQuestionnaireApi
    val userApi: UserApi
}