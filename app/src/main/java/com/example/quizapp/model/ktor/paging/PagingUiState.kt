package com.example.quizapp.model.ktor.paging

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.LoadState.*
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.quizapp.R
import com.example.quizapp.extensions.animateHide
import com.example.quizapp.extensions.animateShow
import com.example.quizapp.model.ListLoadItemType
import com.example.quizapp.view.customimplementations.DataAvailabilityLayout

sealed class PagingUiState {

    object Loading : PagingUiState()

    sealed class PageNotLoading : PagingUiState() {
        object NotEmptyList : PageNotLoading()
        object EmptyListFiltered : PageNotLoading()
        object EmptyListNotFiltered : PageNotLoading()
    }

    sealed class PageError : PagingUiState() {
        object EmptyList : PageError()
        object NotEmptyList : PageError()
    }

    fun adjustUi(
        itemType: ListLoadItemType,
        swipeRefreshLayout: SwipeRefreshLayout,
        dataAvailabilityLayout: DataAvailabilityLayout,
        errorNotEmptyListAction: (() -> (Unit))
    ) {
        when (val state = this) {
            Loading -> {
                swipeRefreshLayout.isRefreshing = true
                dataAvailabilityLayout.animateHide()
            }
            is PageNotLoading -> {
                swipeRefreshLayout.isRefreshing = false
                dataAvailabilityLayout.apply {
                    when (state) {
                        PageNotLoading.EmptyListFiltered -> {
                            setIconWithRes(itemType.icon)
                            setTitleWithRes(itemType.noResultsTitleRes)
                            setTextWithRes(itemType.noResultsTextRes)
                            animateShow()
                        }
                        PageNotLoading.EmptyListNotFiltered -> {
                            setIconWithRes(itemType.icon)
                            setTitleWithRes(itemType.noDataTitleRes)
                            setTextWithRes(itemType.noDataTextRes)
                            animateShow()
                        }
                        PageNotLoading.NotEmptyList -> {
                            animateHide()
                        }
                    }
                }
            }
            is PageError -> {
                swipeRefreshLayout.isRefreshing = false
                when (state) {
                    PageError.EmptyList -> {
                        dataAvailabilityLayout.apply {
                            setIconWithRes(R.drawable.ic_wifi_tethering_error)
                            setTitleWithRes(R.string.errorCouldNotReachBackendTitle)
                            setTextWithRes(R.string.errorCouldNotReachBackendText)
                            animateShow()
                        }
                    }
                    PageError.NotEmptyList -> errorNotEmptyListAction()
                }
            }
        }
    }


    companion object {
        suspend fun fromCombinedLoadStates(
            loadStates: CombinedLoadStates,
            previousLoadState: LoadState?,
            itemCount: Int,
            isFilteredAction: (suspend () -> (Boolean))
        ): PagingUiState? = when (val refresh = loadStates.source.refresh) {
            LoadState.Loading -> Loading
            is NotLoading -> {
                if (loadStates.append.endOfPaginationReached || loadStates.prepend.endOfPaginationReached || refresh.endOfPaginationReached) {
                    if (itemCount == 0 && previousLoadState is LoadState.Loading) {
                        if (isFilteredAction()) {
                            PageNotLoading.EmptyListFiltered
                        } else {
                            PageNotLoading.EmptyListNotFiltered
                        }
                    } else {
                        PageNotLoading.NotEmptyList
                    }
                } else null
            }
            is Error -> {
                if (itemCount == 0) {
                    PageError.EmptyList
                } else {
                    PageError.NotEmptyList
                }
            }
        }
    }
}