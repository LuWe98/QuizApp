package com.example.quizapp.model.ktor.backendsyncer

import androidx.annotation.StringRes
import com.example.quizapp.R

enum class SyncFacultyAndCourseOfStudiesResultType(
    @StringRes val messageRes: Int
) {
    FACULTY_SYNCED(R.string.onlyFacultiesCouldBeSynced),
    FACULTY_ALREADY_UP_TO_DATE(R.string.yourDataIsAlreadyUpToDate),
    BOTH_SYNCED(R.string.syncedFacultyAndCourseOfStudiesData),
    BOTH_ALREADY_UP_TO_DATE(R.string.facultyAndCourseOfStudiesDataAlreadyUpToDate),
    SYNC_UNSUCCESSFUL(R.string.errorCouldNotSyncFacultyAndCosData)
}