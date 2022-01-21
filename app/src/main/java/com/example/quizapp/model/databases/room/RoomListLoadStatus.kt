package com.example.quizapp.model.databases.room

import android.view.View
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.quizapp.model.ListLoadItemType
import com.example.quizapp.model.databases.room.entities.EntityMarker
import com.example.quizapp.view.customimplementations.DataAvailabilityLayout

fun <T> List<T>.asRoomListLoadStatus(
    isListFilteredAction: () -> (Boolean)
): RoomListLoadStatus<T> = isListFilteredAction().let { isFiltered ->
    when {
        isEmpty() && !isFiltered -> RoomListLoadStatus.NoDataAvailable()
        isEmpty() && isFiltered -> RoomListLoadStatus.NoDataFound()
        else -> RoomListLoadStatus.DataFound(this)
    }
}

sealed class RoomListLoadStatus<T>(open val data: List<T> = emptyList()) {

    data class DataFound<T>(override val data: List<T>) : RoomListLoadStatus<T>(data)
    class NoDataFound<T> : RoomListLoadStatus<T>()
    class NoDataAvailable<T> : RoomListLoadStatus<T>()

    fun adjustVisibilities(
        rv: RecyclerView,
        dataAvailabilityLayout: DataAvailabilityLayout,
        itemType: ListLoadItemType
    ) = when (this) {
        is DataFound -> {
            dataAvailabilityLayout.isVisible = false
            rv.visibility = View.VISIBLE
        }
        is NoDataFound -> {
            dataAvailabilityLayout.apply {
                isVisible = true
                setTitleWithRes(itemType.noResultsTitleRes)
                setTextWithRes(itemType.noResultsTextRes)
            }
            rv.visibility = View.INVISIBLE

        }
        is NoDataAvailable -> {
            dataAvailabilityLayout.apply {
                isVisible = true
                setTitleWithRes(itemType.noDataTitleRes)
                setTextWithRes(itemType.noDataTextRes)
            }
            rv.visibility = View.INVISIBLE
        }
    }
}