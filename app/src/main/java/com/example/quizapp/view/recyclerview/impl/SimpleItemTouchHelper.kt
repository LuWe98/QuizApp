package com.example.quizapp.view.recyclerview.impl

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class SimpleItemTouchHelper private constructor(
    val callBack: SimpleItemTouchHelperCallBack
) : ItemTouchHelper(callBack) {

    constructor(
        isLongPressEnabled: Boolean = true,
        isSwipeEnabled: Boolean = true,
        dragFlags: Int = UP or DOWN,
        swipeFlags: Int = START or END
    ) : this(
        SimpleItemTouchHelperCallBack(
            isLongPressEnabled = isLongPressEnabled,
            isSwipeEnabled = isSwipeEnabled,
            dragFlags = dragFlags,
            swipeFlags = swipeFlags
        )
    )

    var onDrag: ((Int, Int) -> (Unit))?
        get() = callBack.onDrag
        set(value) {
            callBack.onDrag = value
        }

    var onDragReleased: ((Int) -> (Unit))?
        get() = callBack.onDragReleased
        set(value) {
            callBack.onDragReleased = value
        }

    var onSwiped: ((Int) -> (Unit))?
        get() = callBack.onSwiped
        set(value) {
            callBack.onSwiped = value
        }

    class SimpleItemTouchHelperCallBack (
        private val isLongPressEnabled: Boolean = true,
        private val isSwipeEnabled: Boolean = true,
        private val dragFlags: Int = UP or DOWN,
        private val swipeFlags: Int = START or END,
    ) : ItemTouchHelper.Callback() {

        var onDrag: ((Int, Int) -> (Unit))? = null
        var onDragReleased: ((Int) -> (Unit))? = null
        var onSwiped: ((Int) -> (Unit))? = null

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
            onDragReleased?.invoke(viewHolder.bindingAdapterPosition)
        }

        override fun isLongPressDragEnabled() = isLongPressEnabled

        override fun isItemViewSwipeEnabled() = isSwipeEnabled
    }
}