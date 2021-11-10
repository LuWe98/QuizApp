package com.example.quizapp.extensions

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.*
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.viewpager2.widget.ViewPager2
import com.example.quizapp.R
import com.example.quizapp.view.recyclerview.impl.CustomItemTouchHelperCallback
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

fun View.makeFullScreen() = apply {
    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, context.resources.displayMetrics.heightPixels)
}

fun TabLayout.attachToViewPager(viewPager: ViewPager2, tabConfigurationStrategy: TabLayoutMediator.TabConfigurationStrategy) =
    TabLayoutMediator(this, viewPager) { tab, pos ->
        tabConfigurationStrategy.onConfigureTab(tab, pos)
    }.also {
        it.attach()
    }


fun TextView.setDrawableSize(size: Int, pos: DrawablePos = DrawablePos.START) {
    val pixelSize = size.px
    compoundDrawablesRelative[pos.ordinal].setBounds(0, 0, pixelSize, pixelSize)
    when(pos){
        DrawablePos.START -> setCompoundDrawablesRelative(compoundDrawablesRelative[pos.ordinal], null, null, null)
        DrawablePos.TOP -> setCompoundDrawablesRelative(null, compoundDrawablesRelative[pos.ordinal], null, null)
        DrawablePos.END -> setCompoundDrawablesRelative(null, null, compoundDrawablesRelative[pos.ordinal], null)
        DrawablePos.BOT -> setCompoundDrawablesRelative(null, null, null, compoundDrawablesRelative[pos.ordinal])
    }
}

enum class DrawablePos{
    START,
    TOP,
    END,
    BOT
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

fun RecyclerView.disableChangeAnimation() {
    (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
}

fun TextView.setTextColorWithRes(@ColorRes colorRes: Int) {
    setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, colorRes)))
}

fun TextView.textAsString() = text.toString()

fun TextView.textAsInt() = text.toString().toInt()

fun TextView.textAsLong() = text.toString().toLong()

fun TextView.textAsFloat() = text.toString().toFloat()

fun TextView.textAsDouble() = text.toString().toDouble()


inline fun EditText.onTextChanged(crossinline action : (String) -> (Unit)) {
    doOnTextChanged { text, _, _, _ ->  action.invoke(text.toString())}
}

inline fun SwitchMaterial.onCheckedChange(crossinline action : (Boolean) -> (Unit)) {
    setOnCheckedChangeListener { _, checked ->  action.invoke(checked)}
}

inline fun ViewPager2.onPageSelected(crossinline action : (Int) -> (Unit)){
    registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            action.invoke(position)
        }
    })
}

inline fun TabLayout.forEachTab(crossinline action: (TabLayout.Tab, Int) -> (Unit)) {
    for (i in 0 .. tabCount){
        getTabAt(i)?.let { tab ->
            action.invoke(tab, i)
        }
    }
}

fun TabLayout.getCustomViewAt(index: Int) = getTabAt(index)?.customView

inline fun TabLayout.onTabSelected(crossinline action: (TabLayout.Tab?) -> Unit) {
    addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) { action(tab) }
        override fun onTabUnselected(tab: TabLayout.Tab?) {}
        override fun onTabReselected(tab: TabLayout.Tab?) {}
    })
}

fun View.findDrawableWith(@DrawableRes res : Int) = ContextCompat.getDrawable(context, res)

fun View.findColor(@ColorRes res : Int) = ContextCompat.getColor(context, res)

fun View.getThemeColor(@AttrRes res : Int) = context.getThemeColor(res)

inline fun View.onClick(crossinline action : () -> (Unit)) {
    setOnClickListener {
        action.invoke()
    }
}

inline fun View.onLongClick(crossinline action : () -> (Unit)) {
    setOnLongClickListener {
        action.invoke()
        return@setOnLongClickListener true
    }
}


fun RadioGroup.getSelectedButton(): RadioButton = findViewById(checkedRadioButtonId)


@SuppressLint("ClickableViewAccessibility")
fun View.enableViewAndChildren(enable: Boolean){
    isEnabled = enable
    if(this is ViewGroup){
//        setOnTouchListener(if(!enable) View.OnTouchListener { _, _ -> false } else View.OnTouchListener { _, _ -> true })
        for(index in 0 .. childCount){
            getChildAt(index)?.enableViewAndChildren(enable)
        }
    }
}