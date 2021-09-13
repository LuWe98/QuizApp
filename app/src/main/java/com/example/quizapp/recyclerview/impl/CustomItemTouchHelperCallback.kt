package com.example.quizapp.recyclerview.impl

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class CustomItemTouchHelperCallback(
    private val dragFlags: Int = ItemTouchHelper.UP or ItemTouchHelper.DOWN,
    private val swipeFlags : Int = ItemTouchHelper.START or ItemTouchHelper.END
) : ItemTouchHelper.Callback() {

    var onDrag : ((Int, Int) -> (Unit))? = null
    var onDragReleased : (() -> (Unit))? = null
    var onSwiped : ((Int) -> (Unit))? = null

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) = makeMovementFlags(dragFlags, swipeFlags)

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        onSwiped?.invoke(viewHolder.bindingAdapterPosition)
    }

    override fun onMove(recyclerView: RecyclerView, fromVh: RecyclerView.ViewHolder, toVh: RecyclerView.ViewHolder): Boolean {
        onDrag?.invoke(fromVh.bindingAdapterPosition, toVh.bindingAdapterPosition)
        return true
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        onDragReleased?.invoke()
    }

    override fun isLongPressDragEnabled() = true

    override fun isItemViewSwipeEnabled() = true
}