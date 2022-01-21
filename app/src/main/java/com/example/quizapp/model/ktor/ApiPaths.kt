package com.example.quizapp.model.ktor

sealed class ApiPaths {

    abstract val root: String

    object UserPaths : ApiPaths() {
        override val root = "/user"

        val LOGIN get() = "/user/login"
        val REGISTER get() = "/user/register"
        val CREATE get() = "/user/admin/create"
        val SYNC get() = "/user/sync"
        val REFRESH_TOKEN get() = "/user/token"
        val DELETE_SELF get() = "/user/delete"
        val DELETE_USER get() = "/user/admin/delete"
        val UPDATE_PASSWORD get() = "/user/update/password"
        val UPDATE_USERNAME get() = "/user/update/username"
        val UPDATE_ROLE get() = "/user/admin/update/role"
        val USERS_PAGED_ADMIN get() = "/users/admin/paged"
        val AUTHORS_PAGED get() = "/authors/paged"
    }

    object QuestionnairePaths : ApiPaths() {
        override val root = "/questionnaire"

        val SYNC get() = "/questionnaire/sync"
        val INSERT get() = "/questionnaires/insert"
        val DELETE get() = "/questionnaire/delete"
        val PAGED get() = "/questionnaires/paged"
        val DOWNLOAD get() = "/questionnaire/download"
        val UPDATE_VISIBILITY get() = "/questionnaire/visibility"
        val SHARE get() = "/questionnaire/share"
    }

    object FilledQuestionnairePaths : ApiPaths() {
        override val root = "/filledQuestionnaire"

        val INSERT_SINGLE get() = "/questionnaire/filled/insert"
        val INSERT_MULTIPLE get() = "/questionnaires/filled/insert"
        val DELETE get() = "/questionnaire/filled/delete"
    }

    object FacultyPaths : ApiPaths() {
        override val root = "/faculty"

        val SYNC get() = "/faculty/sync"
        val INSERT get() = "/faculty/admin/insert"
        val DELETE get() = "/faculty/admin/delete"
    }

    object CourseOfStudiesPaths : ApiPaths() {
        override val root = "/courseOfStudies"

        val SYNC get() = "/courseOfStudies/sync"
        val INSERT get() = "/courseOfStudies/admin/insert"
        val DELETE get() = "/courseOfStudies/admin/delete"
    }
}