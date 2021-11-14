package com.example.quizapp.view.customimplementations.lazytablayout

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max
import kotlin.math.min

@Suppress("UNCHECKED_CAST")
class LazyTabLayout(
    context: Context,
    attributeSet: AttributeSet?,
    defStyle: Int
) : RecyclerView(context, attributeSet, defStyle) {

    constructor(context: Context, attributeSet: AttributeSet) : this(context, attributeSet, 0)
    constructor(context: Context) : this(context, null, 0)

    val layoutManager: LazyTabLayoutManager
        get() = run {
            (getLayoutManager() as LazyTabLayoutManager?) ?: throw IllegalStateException()
        }

    val adapter: LazyTabLayoutAdapter<*, *> get() = (getAdapter() as LazyTabLayoutAdapter<*, *>?) ?: throw IllegalStateException()

    inline fun <reified T : Any> getCurrentTabList(): List<T> = adapter.currentList as List<T>

    inline fun <reified T : LazyTabLayoutAdapter<*, *>> getAdapterCast() = getAdapter() as T

    val itemCount get() = getCurrentTabList<Any>().size

    var selectedTabPosition: Int = 0

    init {
        setLayoutManager(LazyTabLayoutManager(this))
    }

    override fun setAdapter(newAdapter: Adapter<*>?) {
        if (newAdapter !is LazyTabLayoutAdapter<*, *>) throw IllegalStateException()
        super.setAdapter(newAdapter)
    }

    override fun setLayoutManager(newLayoutManager: LayoutManager?) {
        if (newLayoutManager !is LazyTabLayoutManager) throw IllegalStateException()
        super.setLayoutManager(newLayoutManager)
    }

    fun indexOfTab(questionTab: LazyQuestionTab) = adapter.currentList.indexOf(questionTab)

    fun getTabAt(position: Int) = adapter.currentList[position]

    fun clearTabList() {
        if(adapter.currentList.isEmpty()) return
        setTabList(emptyList())
    }

    fun setTabList(tabs: List<Any>) {
        adapter.submitList(tabs as List<Nothing>?)
    }

    fun addTab(lazyTab: Any) {
        val newList = mutableListOf<Any>().apply {
            addAll(getCurrentTabList())
            add(lazyTab)
        }
        adapter.submitList(newList as List<Nothing>?)
    }


//    override fun scrollBy(x: Int, y: Int) {
//        log("SCROLL TO CALLED: $x")
//        if (childCount <= 0) return
//
//        val child = getChildAt(0)
//        val newX = clamp(x, width - paddingRight - paddingLeft, child.width)
//        val newY = clamp(y, height - paddingBottom - paddingTop, child.height)
//        if (newX != scrollX || newY != scrollY) {
//            super.scrollBy(newX, newY)
//        }
//    }
//
//    private fun clamp(n: Int, my: Int, child: Int) = when {
//        my >= child || n < 0 -> 0
//        my + n > child -> child - my
//        else -> n
//    }

    fun updateAllViewHolders() {
        val firstPos = max(0, getViewHolderPositionForChildPosition(0) - 3)
        val lastPos = min(adapter.itemCount - 1, getViewHolderPositionForChildPosition(childCount - 1) + 3)
        for (position in firstPos..lastPos) {
            adapter.notifyItemChanged(position)
        }
    }

    private fun getViewHolderPositionForChildPosition(childPosition: Int) =
        getChildAt(childPosition)?.let { child -> getChildViewHolder(child)?.bindingAdapterPosition } ?: 0
}