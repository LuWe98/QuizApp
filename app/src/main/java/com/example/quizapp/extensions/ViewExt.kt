package com.example.quizapp.extensions

import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.quizapp.R
import com.example.quizapp.recyclerview.impl.CustomItemTouchHelperCallback
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

fun View.makeFullScreen() = apply {
    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, context.resources.displayMetrics.heightPixels)
}

fun applyStatusBarPaddingTop(vararg views: View) {
    views.forEach {
        it.apply {
            setPadding(paddingLeft, paddingTop + context.statusBarHeight, paddingRight, paddingBottom)
        }
    }
}

fun shiftStatusBarHeightTop(vararg views: View) {
    views.forEach {
        it.apply {
            (it.layoutParams as ViewGroup.MarginLayoutParams).apply {
//                setMargins(marginLeft, marginTop - it.context.statusBarHeight, marginRight, marginBottom)
//                it.context.showToast("MARGIN TOP $marginTop")
            }
        }
    }
}

fun TabLayout.attachToViewPager(viewPager: ViewPager2, tabConfigurationStrategy: TabLayoutMediator.TabConfigurationStrategy) =
    TabLayoutMediator(this, viewPager) { tab, pos ->
        tabConfigurationStrategy.onConfigureTab(tab, pos)
    }.also {
        it.attach()
    }


fun TextView.setDrawableSize(size: Int, pos: Int = 0) {
    val pixelSize = size.px
    compoundDrawablesRelative[pos].setBounds(0, 0, pixelSize, pixelSize)
    setCompoundDrawablesRelative(compoundDrawablesRelative[pos], null, null, null)
}

fun ProgressBar.setProgressWithAnimation(to: Int, duration: Long = context.resources.getInteger(R.integer.defaultProgressAnimDuration).toLong()) {
    ObjectAnimator.ofInt(this, "progress", progress, to).apply {
        interpolator = LinearInterpolator()
        setDuration(duration)
        start()
    }
}

fun ProgressBar.setSecondaryProgressWithAnimation(to: Int, duration: Long = context.resources.getInteger(R.integer.defaultProgressAnimDuration).toLong()) {
    ObjectAnimator.ofInt(this, "secondaryProgress", secondaryProgress, to).apply {
        interpolator = LinearInterpolator()
        setDuration(duration)
        start()
    }
}

fun ImageView.setDrawableTint(@ColorInt color: Int) {
    this.imageTintList = ColorStateList.valueOf(color)
}

fun ImageView.setDrawableTintWithRes(@ColorRes color: Int) {
    this.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, color))
}

fun ImageView.setImageDrawable(@DrawableRes drawableRes: Int) {
    setImageDrawable(ContextCompat.getDrawable(context, drawableRes))
}

fun RecyclerView.updateAllViewHolders() {
    adapter?.let {
        val firstPos: Int = kotlin.math.max(0, getViewHolderPositionForChildPosition(0) - 3)
        val lastPos: Int = kotlin.math.min(it.itemCount - 1, getViewHolderPositionForChildPosition(childCount - 1) + 3)
        for (position in firstPos..lastPos) {
            it.notifyItemChanged(position)
        }
    }
}

fun RecyclerView.getViewHolderPositionForChildPosition(childPosition: Int) =
    getChildAt(childPosition)?.let { child -> getChildViewHolder(child)?.bindingAdapterPosition } ?: 0

fun RecyclerView.addCustomItemTouchHelperCallBack(
    dragFlags: Int = ItemTouchHelper.UP or ItemTouchHelper.DOWN,
    scrollFlags: Int = ItemTouchHelper.START or ItemTouchHelper.END
) = CustomItemTouchHelperCallback(dragFlags, scrollFlags).apply {
    ItemTouchHelper(this).also {
        it.attachToRecyclerView(this@addCustomItemTouchHelperCallBack)
    }
}

fun TextView.setTextColorWithRes(@ColorRes colorRes: Int) {
    setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, colorRes)))
}

fun TextView.textAsString() = text.toString()

fun TextView.textAsInt() = text.toString().toInt()

fun TextView.textAsLong() = text.toString().toLong()

fun TextView.textAsFloat() = text.toString().toFloat()

fun TextView.textAsDouble() = text.toString().toDouble()

fun EditText.onTextChanged(action : (String) -> (Unit)) {
    doOnTextChanged { text, _, _, _ ->  action.invoke(text.toString())}
}


