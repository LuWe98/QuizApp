package com.example.quizapp.model.ktor

sealed class ApiPaths {

    object UserPaths : ApiPaths() {
        const val LOGIN = "/user/login"
        const val REGISTER = "/user/register"
        const val CREATE = "/admin/user/create"
        const val SYNC = "/user/sync"
        const val REFRESH_TOKEN = "/user/token"
        const val UPDATE_USERNAME = "/user/update/username"
        const val UPDATE_ROLE = "/admin/user/update/role"
        const val DELETE_SELF = "/user/delete"
        const val DELETE_USER = "/admin/users/delete"
        const val AUTHORS_PAGED = "/authors/paged"
        const val CHANGE_PASSWORD = "/user/changepassword"
        const val USERS_PAGED_ADMIN = "/admin/users/paged"
        const val GENERATE_RANDOM = "/user/random"
    }

    object FacultyPaths : ApiPaths() {
        const val SYNC = "/faculty/sync"
        const val INSERT = "/admin/faculty/insert"
        const val DELETE = "/admin/faculty/delete"
    }

    object CourseOfStudiesPaths : ApiPaths() {
        const val SYNC = "/courseOfStudies/sync"
        const val INSERT = "/admin/courseOfStudies/insert"
        const val DELETE = "/admin/courseOfStudies/delete"
    }


    object QuestionnairePaths : ApiPaths() {
        const val SYNC = "/questionnaire/sync"
        const val INSERT = "/questionnaires/insert"
        const val DELETE = "/questionnaire/delete"
        const val PAGED = "/questionnaires/paged"
        const val DOWNLOAD = "/questionnaire/download"
        const val UPDATE_VISIBILITY = "/questionnaire/visibility"
        const val SHARE = "/questionnaire/share"
        const val GENERATE_RANDOM = "/questionnaire/random"
    }

    object FilledQuestionnairePaths : ApiPaths() {
        const val INSERT = "/questionnaire/filled/insert"
        const val INSERTS = "/questionnaires/filled/insert"
        const val DELETE = "/questionnaire/filled/delete"
        const val GENERATE_RANDOM = "/questionnaire/filled/random"
    }

}