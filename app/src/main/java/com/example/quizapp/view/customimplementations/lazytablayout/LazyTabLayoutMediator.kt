package com.example.quizapp.view.customimplementations.lazytablayout

import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.min
import kotlin.math.roundToInt

class LazyTabLayoutMediator<T: Any> (
    private val lazyTabLayout: LazyTabLayout,
    private val viewPager: ViewPager2,
    private val provideTabAction: ((Int) -> T)
) {

    private val lazyTabLayoutManager get() = lazyTabLayout.layoutManager

    private val itemCount get() = lazyTabLayoutManager.itemCount

    private val viewPagerAdapter: RecyclerView.Adapter<*>
        get() = run { viewPager.adapter ?: throw IllegalStateException() }

    private var onPageChangedCallback: LazyViewPagerOnPageChangedCallback? = null


    fun attach() {
        populateTabsFromPagerAdapter()

        onPageChangedCallback = LazyViewPagerOnPageChangedCallback().also { callback ->
            viewPager.registerOnPageChangeCallback(callback)
        }
    }

    fun detach() {
        onPageChangedCallback?.let { callback ->
            viewPager.unregisterOnPageChangeCallback(callback)
        }
    }

    private fun populateTabsFromPagerAdapter() {
        val viewPagerItemCount = viewPagerAdapter.itemCount
        lazyTabLayout.clearTabList()
        lazyTabLayout.setTabList(MutableList(viewPagerItemCount) {
            provideTabAction.invoke(it)
        })

        if (viewPagerItemCount > 0) {
            val lastItem: Int = itemCount - 1
            val currItem = min(viewPager.currentItem, lastItem)
            if (currItem != lazyTabLayout.selectedTabPosition) {
                lazyTabLayout.selectedTabPosition = currItem
            }
        }
    }

    private inner class LazyViewPagerOnPageChangedCallback : ViewPager2.OnPageChangeCallback() {

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            val roundedPosition = (position + positionOffset).roundToInt()
            if (roundedPosition < 0 || roundedPosition >= itemCount) {
                return
            }

            calculateScrollXForViewHolder(position, positionOffset).let { scrollXValue ->
                lazyTabLayout.scrollBy(scrollXValue, 0)
            }
        }

        override fun onPageSelected(position: Int) {
            lazyTabLayout.selectedTabPosition = position
            lazyTabLayout.scrollToPosition(position)
            lazyTabLayout.updateAllViewHolders()
        }
    }


    private fun calculateScrollXForViewHolder(position: Int, positionOffset: Float): Int {
        if (position > lazyTabLayoutManager.findLastVisibleItemPosition() || position < lazyTabLayoutManager.findFirstVisibleItemPosition()) {
            lazyTabLayout.scrollToPosition(position)
            return 0
        }

        val selectedChild = lazyTabLayout.findViewHolderForAdapterPosition(position)?.itemView ?: return 0
        val nextChild = if (position + 1 < itemCount) lazyTabLayout.findViewHolderForAdapterPosition(position + 1)?.itemView else null
        val selectedWidth = selectedChild.width
        val nextWidth = nextChild?.width ?: 0
        val scrollBase = selectedChild.left + selectedWidth / 2 - lazyTabLayoutManager.width / 2
        val scrollOffset = ((selectedWidth + nextWidth) * 0.5f * positionOffset).toInt()
        return scrollBase + scrollOffset
    }
}