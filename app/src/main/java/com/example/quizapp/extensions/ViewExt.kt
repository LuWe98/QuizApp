package com.example.quizapp.extensions

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.*
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.viewpager2.widget.ViewPager2
import com.example.quizapp.R
import com.example.quizapp.databinding.ChipEntryBinding
import com.example.quizapp.view.recyclerview.impl.SimpleItemTouchHelper
import com.google.android.material.chip.ChipGroup
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

fun View.setBackgroundColorWithRes(@ColorRes res: Int) = setBackgroundColor(ContextCompat.getColor(context, res))

fun View.setBackgroundTint(@ColorInt colorInt: Int) {
    backgroundTintList = ColorStateList.valueOf(colorInt)
}

fun View.setBackgroundTintWithRes(@ColorRes res: Int) {
    setBackgroundTint(ContextCompat.getColor(context, res))
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
    when (pos) {
        DrawablePos.START -> setCompoundDrawablesRelative(compoundDrawablesRelative[pos.ordinal], null, null, null)
        DrawablePos.TOP -> setCompoundDrawablesRelative(null, compoundDrawablesRelative[pos.ordinal], null, null)
        DrawablePos.END -> setCompoundDrawablesRelative(null, null, compoundDrawablesRelative[pos.ordinal], null)
        DrawablePos.BOT -> setCompoundDrawablesRelative(null, null, null, compoundDrawablesRelative[pos.ordinal])
    }
}

enum class DrawablePos {
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


inline fun EditText.onTextChanged(crossinline action: (String) -> (Unit)) {
    doOnTextChanged { text, _, _, _ -> action.invoke(text.toString()) }
}

inline fun ImageView.changeSearchIconOnCondition(
    animDuration: Long = 150,
    crossinline condition: () -> (Boolean),
) = changeIconOnCondition(R.drawable.ic_search, R.drawable.ic_cross, animDuration, condition)

inline fun ImageView.changeIconOnCondition(
    @DrawableRes trueIcon: Int = R.drawable.ic_search,
    @DrawableRes falseIcon: Int = R.drawable.ic_cross,
    animDuration: Long = 150,
    crossinline condition: () -> (Boolean),
) {
    val cond = condition()
    if (tag == cond) return

    if (tag == null) {
        tag = cond
        setImageDrawable(if (cond) trueIcon else falseIcon)
        return
    }

    tag = cond
    clearAnimation()
    animate().scaleX(0f)
        .scaleY(0f)
        .setInterpolator(AccelerateInterpolator())
        .setDuration(animDuration)
        .withEndAction {
            setImageDrawable(if (cond) trueIcon else falseIcon)
            animate().scaleY(1f)
                .scaleX(1f)
                .setInterpolator(DecelerateInterpolator())
                .setDuration(animDuration)
                .start()
        }.start()
}


inline fun SwitchMaterial.onCheckedChange(crossinline action: (Boolean) -> (Unit)) {
    setOnCheckedChangeListener { _, checked -> action.invoke(checked) }
}

inline fun ViewPager2.onPageSelected(crossinline action: (Int) -> (Unit)) {
    registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            action.invoke(position)
        }
    })
}

inline fun TabLayout.forEachTab(crossinline action: (TabLayout.Tab, Int) -> (Unit)) {
    for (i in 0..tabCount) {
        getTabAt(i)?.let { tab ->
            action.invoke(tab, i)
        }
    }
}

fun TabLayout.getCustomViewAt(index: Int) = getTabAt(index)?.customView

inline fun TabLayout.onTabSelected(crossinline action: (TabLayout.Tab?) -> Unit) {
    addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            action(tab)
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {}
        override fun onTabReselected(tab: TabLayout.Tab?) {}
    })
}

fun View.findDrawableWith(@DrawableRes res: Int) = ContextCompat.getDrawable(context, res)

fun View.findColor(@ColorRes res: Int) = ContextCompat.getColor(context, res)

fun View.getThemeColor(@AttrRes res: Int) = context.getThemeColor(res)

inline fun View.onClick(crossinline action: () -> (Unit)) {
    setOnClickListener {
        action.invoke()
    }
}

inline fun View.onLongClick(crossinline action: () -> (Unit)) {
    setOnLongClickListener {
        action.invoke()
        return@setOnLongClickListener true
    }
}

@SuppressLint("ClickableViewAccessibility")
inline fun View.onTouch(crossinline  action: (MotionEvent) -> Unit) {
    setOnTouchListener { _, event ->
        action.invoke(event)
        return@setOnTouchListener true
    }
}


fun RadioGroup.getSelectedButton(): RadioButton = findViewById(checkedRadioButtonId)


fun View.enableViewAndChildren(enable: Boolean) {
    isEnabled = enable
    if (this is ViewGroup) {
        for (index in 0..childCount) {
            getChildAt(index)?.enableViewAndChildren(enable)
        }
    }
}


inline fun <reified T> ChipGroup.setUpChipsForChipGroup(
    list: Collection<T>,
    crossinline textProvider: (T) -> (String),
    crossinline onClickCallback: (T) -> (Unit) = {},
    crossinline onLongClickCallback: (T) -> (Unit) = {}
) {
    val mapped: Set<T> = children.mapNotNull { if (it.tag is T) it.tag as T else null }.toSet()
    val itemsToInsert: Set<T> = list.toSet() - mapped
    val itemsToRemove: Set<T> = mapped - list.toSet() - itemsToInsert

    itemsToRemove.forEach { tagToFind ->
        children.firstOrNull { it.tag == tagToFind }?.let(::removeView)
    }

    itemsToInsert.forEach { entry ->
        ChipEntryBinding.inflate(LayoutInflater.from(context)).root.apply {
            tag = entry
            text = textProvider.invoke(entry)
            onClick { onClickCallback.invoke(this.tag as T) }
            onLongClick { onLongClickCallback.invoke(this.tag as T) }
        }.let {
            addView(it, 0)
        }
    }
}