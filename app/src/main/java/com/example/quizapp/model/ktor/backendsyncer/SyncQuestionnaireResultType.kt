package com.example.quizapp.model.ktor.backendsyncer

import androidx.annotation.StringRes
import com.example.quizapp.R

enum class SyncQuestionnaireResultType(
    @StringRes val messageRes: Int
) {

    DATA_SYNCED(R.string.questionnaireDataSynced),
    NOT_SUCCESSFUL(R.string.errorCouldNotSyncQuestionnaires)

}