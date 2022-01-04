package com.example.quizapp.utils

import android.view.View
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.quizapp.view.customimplementations.DataAvailabilityLayout

enum class RemoteDataAvailability {

    ENTRIES_FOUND,
    NO_ENTRIES_FOUND,
    NO_ENTRIES_EXIST;

    fun adjustVisibilities(
        rv: RecyclerView,
        dataAvailabilityLayout: DataAvailabilityLayout,
        @StringRes notFoundTitleRes: Int,
        @StringRes notFoundTextRes: Int,
        @StringRes notExistsTitleRes: Int,
        @StringRes notExistsTextRes: Int
    ) = when (this) {
        ENTRIES_FOUND -> {
            dataAvailabilityLayout.isVisible = false
            rv.visibility = View.VISIBLE
        }
        NO_ENTRIES_FOUND -> {
            dataAvailabilityLayout.apply {
                isVisible = true
                setTitleWithRes(notFoundTitleRes)
                setTextWithRes(notFoundTextRes)
            }
            rv.visibility = View.INVISIBLE

        }
        NO_ENTRIES_EXIST -> {
            dataAvailabilityLayout.apply {
                isVisible = true
                setTitleWithRes(notExistsTitleRes)
                setTextWithRes(notExistsTextRes)
            }
            rv.visibility = View.INVISIBLE
        }
    }
}