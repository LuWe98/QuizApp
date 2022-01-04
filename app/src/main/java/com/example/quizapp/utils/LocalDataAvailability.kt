package com.example.quizapp.utils

import android.view.View
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.quizapp.view.customimplementations.DataAvailabilityLayout

fun <T> List<T>.asLocalDataAvailability(
    isListFilteredAction: () -> (Boolean)
): LocalDataAvailability<List<T>> {
    val isFiltered = isListFilteredAction()
    return when {
        isEmpty() && !isFiltered -> LocalDataAvailability.NoDataExists()
        isEmpty() && isFiltered -> LocalDataAvailability.NoDataFound()
        else -> LocalDataAvailability.DataFound(this)
    }
}

sealed class LocalDataAvailability<out T>(open val data: T? = null) {

    data class DataFound<T>(override val data: T) : LocalDataAvailability<T>(data)
    class NoDataFound<T> : LocalDataAvailability<T>()
    class NoDataExists<T> : LocalDataAvailability<T>()

    fun adjustVisibilities(
        rv: RecyclerView,
        dataAvailabilityLayout: DataAvailabilityLayout,
        @StringRes notFoundTitleRes: Int,
        @StringRes notFoundTextRes: Int,
        @StringRes notExistsTitleRes: Int,
        @StringRes notExistsTextRes: Int
    ) = when (this) {
        is DataFound -> {
            dataAvailabilityLayout.isVisible = false
            rv.visibility = View.VISIBLE
        }
        is NoDataFound -> {
            dataAvailabilityLayout.apply {
                isVisible = true
                setTitleWithRes(notFoundTitleRes)
                setTextWithRes(notFoundTextRes)
            }
            rv.visibility = View.INVISIBLE

        }
        is NoDataExists -> {
            dataAvailabilityLayout.apply {
                isVisible = true
                setTitleWithRes(notExistsTitleRes)
                setTextWithRes(notExistsTextRes)
            }
            rv.visibility = View.INVISIBLE
        }
    }
}