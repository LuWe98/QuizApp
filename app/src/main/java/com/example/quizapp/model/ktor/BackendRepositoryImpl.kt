package com.example.quizapp.model.ktor

import com.example.quizapp.model.ktor.apiclasses.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackendRepositoryImpl @Inject constructor(
    override val userApi: UserApi,
    override val questionnaireApi: QuestionnaireApi,
    override val filledQuestionnaireApi: FilledQuestionnaireApi,
    override val facultyApi: FacultyApi,
    override val courseOfStudiesApi: CourseOfStudiesApi
) : BackendRepository