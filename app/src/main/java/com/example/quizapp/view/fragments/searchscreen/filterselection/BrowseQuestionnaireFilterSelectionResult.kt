package com.example.quizapp.view.fragments.searchscreen.filterselection

import android.os.Parcelable
import com.example.quizapp.model.databases.properties.AuthorInfo
import kotlinx.parcelize.Parcelize

@Parcelize
data class BrowseQuestionnaireFilterSelectionResult(
    val selectedAuthors: Set<AuthorInfo>
): Parcelable