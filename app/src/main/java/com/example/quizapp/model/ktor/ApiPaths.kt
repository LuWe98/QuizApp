package com.example.quizapp.model.ktor

sealed class ApiPaths {

    object UserPaths : ApiPaths() {
        const val LOGIN = "/user/login"
        const val REGISTER = "/user/register"
        const val CREATE = "/user/admin/create"
        const val SYNC = "/user/sync"
        const val REFRESH_TOKEN = "/user/token"
        const val DELETE_SELF = "/user/delete"
        const val DELETE_USER = "/user/admin/delete"
        const val UPDATE_PASSWORD = "/user/update/password"
        const val UPDATE_USERNAME = "/user/update/username"
        const val UPDATE_ROLE = "/user/admin/update/role"
        const val AUTHORS_PAGED = "/authors/paged"
        const val USERS_PAGED_ADMIN = "/users/admin/paged"
    }

    object QuestionnairePaths : ApiPaths() {
        const val SYNC = "/questionnaire/sync"
        const val INSERT = "/questionnaires/insert"
        const val DELETE = "/questionnaire/delete"
        const val PAGED = "/questionnaires/paged"
        const val DOWNLOAD = "/questionnaire/download"
        const val UPDATE_VISIBILITY = "/questionnaire/visibility"
        const val SHARE = "/questionnaire/share"
    }

    object FilledQuestionnairePaths : ApiPaths() {
        const val INSERT_SINGLE = "/questionnaire/filled/insert"
        const val INSERT_MULTIPLE = "/questionnaires/filled/insert"
        const val DELETE = "/questionnaire/filled/delete"
    }

    object FacultyPaths : ApiPaths() {
        const val SYNC = "/faculty/sync"
        const val INSERT = "/faculty/admin/insert"
        const val DELETE = "/faculty/admin/delete"
    }

    object CourseOfStudiesPaths : ApiPaths() {
        const val SYNC = "/courseOfStudies/sync"
        const val INSERT = "/courseOfStudies/admin/insert"
        const val DELETE = "/courseOfStudies/admin/delete"
    }

}