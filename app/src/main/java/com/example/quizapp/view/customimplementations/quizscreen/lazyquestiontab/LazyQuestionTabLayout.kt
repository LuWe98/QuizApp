package com.example.quizapp.view.customimplementations.quizscreen.lazyquestiontab

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import java.lang.ref.WeakReference
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Suppress("UNCHECKED_CAST")
class LazyQuestionTabLayout(
    context: Context,
    attributeSet: AttributeSet?,
    defStyle: Int
) : RecyclerView(context, attributeSet, defStyle) {

    constructor(context: Context, attributeSet: AttributeSet) : this(context, attributeSet, 0)
    constructor(context: Context) : this(context, null, 0)

    private var attachedViewPagerRef: WeakReference<ViewPager2>? = null

    private val attachedViewPager: ViewPager2? get() = attachedViewPagerRef?.get()

    private val attachedViewPagerItemCount: Int get() = attachedViewPager?.adapter?.itemCount ?: 0

    private var onPageChangedCallback: LazyQuestionTabLayoutOnPageChangedCallback? = null

    val layoutManager: LazyQuestionTabLayoutManager
        get() = run {
            (getLayoutManager() as LazyQuestionTabLayoutManager?) ?: throw IllegalStateException()
        }

    val lazyTabAdapter get() = adapter as LazyQuestionTabLayoutAdapter<*, *>

    val itemCount get() = lazyTabAdapter.currentList.size

    val currentItem: Int get() = attachedViewPager?.currentItem ?: NO_POSITION

    init {
        setLayoutManager(LazyQuestionTabLayoutManager(context))
    }

    override fun setAdapter(newAdapter: Adapter<*>?) {
        if (newAdapter !is LazyQuestionTabLayoutAdapter<*, *>) throw IllegalStateException()
        super.setAdapter(newAdapter)
    }

    override fun setLayoutManager(newLayoutManager: LayoutManager?) {
        if (newLayoutManager !is LazyQuestionTabLayoutManager) throw IllegalStateException()
        super.setLayoutManager(newLayoutManager)
    }

    private fun clearTabList() {
        if (lazyTabAdapter.currentList.isEmpty()) return
        setTabList(emptyList())
    }

    private fun setTabList(tabs: List<Any>) {
        lazyTabAdapter.submitList(tabs as List<Nothing>?)
    }

    fun updateAllViewHolders() {
        val firstPos = max(0, getViewHolderPositionForChildPosition(0) - 3)
        val lastPos = min(lazyTabAdapter.itemCount - 1, getViewHolderPositionForChildPosition(childCount - 1) + 3)
        for (position in firstPos..lastPos) {
            lazyTabAdapter.notifyItemChanged(position)
        }
    }

    private fun getViewHolderPositionForChildPosition(childPosition: Int) =
        getChildAt(childPosition)?.let { child -> getChildViewHolder(child)?.bindingAdapterPosition } ?: 0


    fun attachToViewPager(viewPager: ViewPager2) {
        attachedViewPagerRef = WeakReference(viewPager)

        onPageChangedCallback = LazyQuestionTabLayoutOnPageChangedCallback().also { callback ->
            viewPager.registerOnPageChangeCallback(callback)
        }
    }

    fun <T : Any> attachToViewPagerAndPopulate(viewPager: ViewPager2, provideTabAction: ((Int) -> T)) {
        attachedViewPagerRef = WeakReference(viewPager)

        populateTabsFromPagerAdapter(provideTabAction)

        onPageChangedCallback = LazyQuestionTabLayoutOnPageChangedCallback().also { callback ->
            viewPager.registerOnPageChangeCallback(callback)
        }
    }

    fun detachFromViewPager() {
        clearTabList()
        onPageChangedCallback?.let { callback ->
            attachedViewPager?.unregisterOnPageChangeCallback(callback)
        }
        attachedViewPagerRef = null
    }


    fun <T : Any> populateTabsFromPagerAdapter(provideTabAction: ((Int) -> T)) {
        clearTabList()
        setTabList(MutableList(attachedViewPagerItemCount, provideTabAction::invoke))
    }

    private inner class LazyQuestionTabLayoutOnPageChangedCallback : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            val roundedPosition = (position + positionOffset).roundToInt()
            if (roundedPosition < 0 || roundedPosition >= itemCount) {
                return
            }
            scrollBy(calculateScrollXForViewHolder(position, positionOffset), 0)
        }

        override fun onPageSelected(position: Int) {
            scrollToPosition(position)
            updateAllViewHolders()
        }
    }

    private fun calculateScrollXForViewHolder(position: Int, positionOffset: Float): Int {
        if (position > layoutManager.findLastVisibleItemPosition() || position < layoutManager.findFirstVisibleItemPosition()) {
            scrollToPosition(position)
            return 0
        }

        val selectedChild = findViewHolderForAdapterPosition(position)?.itemView ?: return 0
        val nextChild = if (position + 1 < itemCount) findViewHolderForAdapterPosition(position + 1)?.itemView else null
        val selectedWidth = selectedChild.width
        val nextWidth = nextChild?.width ?: 0
        val scrollBase = selectedChild.left + selectedWidth / 2 - layoutManager.width / 2
        val scrollOffset = ((selectedWidth + nextWidth) * 0.5f * positionOffset).toInt()
        return scrollBase + scrollOffset
    }
}