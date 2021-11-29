package com.example.quizapp.view.fragments.searchscreen.filterselection

import android.os.Parcelable
import com.example.quizapp.model.databases.mongodb.documents.user.AuthorInfo
import kotlinx.parcelize.Parcelize

@Parcelize
data class BrowseQuestionnaireFilterSelectionResult(
    val selectedCosIds: Set<String>,
    val selectedFacultyIds: Set<String>,
    val selectedAuthors: Set<AuthorInfo>
): Parcelable