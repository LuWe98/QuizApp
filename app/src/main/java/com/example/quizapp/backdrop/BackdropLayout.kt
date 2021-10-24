package com.example.quizapp.backdrop

import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.FrameLayout
import androidx.annotation.DimenRes
import androidx.core.view.isVisible
import com.example.quizapp.R
import kotlin.math.max
import kotlin.math.min

class BackdropLayout constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0, 0)
    constructor(context: Context) : this(context, null, 0, 0)

    companion object {
        private const val NO_FRONT_LAYER_TOP_MARGIN = 0
        private const val DEFAULT_ANIM_DURATION = 300
        private const val BACK_LAYER_INDEX = 0
        private const val FRONT_LAYER_INDEX = 1

        private const val DEFAULT_TRANSLATION = 0
        private const val DEFAULT_SCALE = 1f
        private const val DEFAULT_OFFSET = 0f
        private const val DEFAULT_MIN_FRONT_LAYER_HEIGHT = 0f


        private const val MIN_PROGRESS = 0f
        private const val MAX_PROGRESS = 1f
        private const val Y_COORD_INDEX = 1

        private const val NO_DESIRED_COLLAPSED_TRANSLATION = -1f

        private const val STATE_KEY = "stateKey"
        private const val EXPANDED_KEY = "expandedKey"
    }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.BackdropLayout, 0, 0).let { typedArray ->
            animationDuration = typedArray.getInteger(R.styleable.BackdropLayout_animationDuration, DEFAULT_ANIM_DURATION).toLong()
            frontLayerTopMargin = typedArray.getDimension(R.styleable.BackdropLayout_frontLayerTopMargin, NO_FRONT_LAYER_TOP_MARGIN.toFloat()).toInt()
            frontLayerMinHeight = typedArray.getDimension(R.styleable.BackdropLayout_frontLayerMinHeight, DEFAULT_MIN_FRONT_LAYER_HEIGHT).toInt()
            frontLayerOffset = typedArray.getDimension(R.styleable.BackdropLayout_frontLayerOffset, DEFAULT_OFFSET).toInt()
            frontLayerScale = typedArray.getFloat(R.styleable.BackdropLayout_frontLayerScale, DEFAULT_SCALE)
            typedArray.recycle()
        }
    }


    private val animListeners = mutableListOf<BackDropAnimListener>()

    private val stateListeners = mutableListOf<BackdropStateListener>()

    private val backLayer get() = getChildAt(BACK_LAYER_INDEX) as ViewGroup

    private val frontLayer get() = getChildAt(FRONT_LAYER_INDEX) as ViewGroup

    var frontLayerScrimView: View? = null
        set(value) {
            field = value?.apply {
                isVisible = !expanded
                isClickable = !expanded
                isFocusable = !expanded
                setOnClickListener { toggle() }
            }
        }


    private var frontLayerTopMargin: Int

    private var frontLayerMinHeight: Int

    private var frontLayerOffset: Int

    private var frontLayerScale: Float

    private var animationDuration: Long

    private var expanded = true

    private var currentAnimator: ViewPropertyAnimator? = null

    var animationInterpolator: Interpolator = DecelerateInterpolator()


    private var desiredCollapsedTranslation = NO_DESIRED_COLLAPSED_TRANSLATION

    private val backLayerHeight get() = backLayer.measuredHeight

    private val desiredFrontLayerHeight get() = measuredHeight - frontLayerTopMargin

    private val calculatedTranslation get() = max(backLayerHeight - frontLayerTopMargin, 0) + frontLayerOffset

    private val maxTranslation get() = desiredFrontLayerHeight - frontLayerMinHeight

    private val desiredCollapsedHeight get() = max(desiredCollapsedTranslation.toInt() - frontLayerTopMargin, 0) + frontLayerOffset

    private val frontLayerScaleOffset get() = (frontLayer.measuredHeight * (1 - frontLayerScale)).toInt()


    private val collapsedTranslation
        get() = min(
            (if (desiredCollapsedTranslation == NO_DESIRED_COLLAPSED_TRANSLATION) calculatedTranslation
            else desiredCollapsedHeight) - (frontLayerScaleOffset / 2), maxTranslation
        )


    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)
        if (child !is ViewGroup) {
            throw IllegalArgumentException("All children have to be ViewGroups!")
        }

        child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)

        when (childCount - 1) {
            BACK_LAYER_INDEX -> initBackLayerHierarchyListener()
            FRONT_LAYER_INDEX -> if (frontLayerTopMargin != NO_FRONT_LAYER_TOP_MARGIN) {
                setFrontLayerTopMargin(frontLayerTopMargin)
            }
            else -> throw IllegalStateException("BackDropLayout can only have two children!")
        }
    }


    fun setFrontLayerTopAnchor(backLayerTopAnchor: View) {
        backLayer.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                backLayer.viewTreeObserver.removeOnGlobalLayoutListener(this)
                var margin = backLayerTopAnchor.bottom
                if (backLayerTopAnchor.layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
                    val coord = IntArray(2).apply { backLayerTopAnchor.getLocationOnScreen(this) }
                    margin -= max(coord[Y_COORD_INDEX] - backLayerTopAnchor.top, NO_FRONT_LAYER_TOP_MARGIN)
                }
                setFrontLayerTopMarginInternal(margin)
            }
        })
    }

    fun setFrontLayerBotAnchor(backLayerBotAnchor: View) {
        backLayer.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                backLayer.viewTreeObserver.removeOnGlobalLayoutListener(this)
                desiredCollapsedTranslation = backLayerBotAnchor.bottom.toFloat()
            }
        })
    }


    fun setFrontLayerTopMargin(marginTop: Int) {
        setFrontLayerTopMarginInternal(marginTop)
    }

    fun setFrontLayerTopMarginWithRes(@DimenRes dimenRes: Int) {
        setFrontLayerTopMarginInternal(context.resources.getDimension(dimenRes).toInt())
    }

    private fun setFrontLayerTopMarginInternal(newTopMargin: Int) {
        frontLayerTopMargin = newTopMargin

        frontLayer.apply {
            layoutParams = (layoutParams as LayoutParams).apply {
                topMargin = newTopMargin
            }
        }
    }



    private val adjustableViewPropertyAnimator: (Int) -> (ViewPropertyAnimator) = { translationAmount ->
        frontLayer.animate()
            .translationY(translationAmount.toFloat())
            .setDuration(animationDuration)
            .setInterpolator(animationInterpolator)
            .setUpdateListener { }
            .withStartAction { }
            .withEndAction { }
    }

    private val regularViewPropertyAnimator: (Int) -> (ViewPropertyAnimator) = { translationAmount ->
        frontLayer.animate()
            .translationY(translationAmount.toFloat())
            .scale(if (expanded) DEFAULT_SCALE else frontLayerScale)
            .setDuration(animationDuration)
            .setInterpolator(animationInterpolator)
            .setUpdateListener { onFrontLayerAnimUpdate(it) }
            .withStartAction { onFrontLayerAnimationStart() }
            .withEndAction { onFrontLayerAnimationEnd() }
    }


    private fun ViewPropertyAnimator.scale(scaleValue: Float): ViewPropertyAnimator = apply {
        scaleY(scaleValue)
        scaleX(scaleValue)
    }

    private fun onFrontLayerAnimUpdate(valueAnimator: ValueAnimator) {
        valueAnimator.getAnimProgress(expanded).let { progress ->
            frontLayerScrimView?.alpha = progress
            animListeners.forEach { it.onProgressChanged(progress) }
        }
    }

    private fun ValueAnimator.getAnimProgress(inverted: Boolean = false) =
        (currentPlayTime / duration.toFloat()).let {
            min(max(if (inverted) 1 - it else it, MIN_PROGRESS), MAX_PROGRESS)
        }

    private fun onFrontLayerAnimationStart() {
        frontLayerScrimView?.apply {
            isVisible = true
            isClickable = !expanded
            isFocusable = !expanded
        }
    }

    private fun onFrontLayerAnimationEnd() {
        currentAnimator = null
        animListeners.forEach { it.onPostAnimation(expanded) }
        stateListeners.forEach { it.onPostAnimation(expanded) }
        frontLayerScrimView?.apply {
            isVisible = !expanded
            isClickable = !expanded
            isFocusable = !expanded
        }
    }




    private fun animateFrontLayer(translationAmount: Int, animator: (Int) -> (ViewPropertyAnimator) = regularViewPropertyAnimator) {
        currentAnimator?.let {
            it.cancel()
            currentAnimator = null
            frontLayer.clearAnimation()
        }

        currentAnimator = animator.invoke(translationAmount).apply {
            start()
        }
    }


    private fun forceFrontLayer(translationAmount: Int) {
        frontLayer.apply {
            translationY = translationAmount.toFloat()
            val scaleValue = if (expanded) DEFAULT_SCALE else frontLayerScale
            scaleX = scaleValue
            scaleY = scaleValue
        }
        frontLayerScrimView?.isVisible = !expanded
        stateListeners.forEach { it.onPostAnimation(expanded) }
    }


    private fun initBackLayerHierarchyListener() {
        backLayer.setOnHierarchyChangeListener(object : OnHierarchyChangeListener {
            override fun onChildViewAdded(parent: View?, addedView: View?) {
                adjustFrontLayer()
            }
            override fun onChildViewRemoved(parent: View?, removedView: View?) {
                adjustFrontLayer()
            }
        })
    }


    fun adjustFrontLayer() {
        if (expanded) return

        backLayer.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                backLayer.viewTreeObserver.removeOnGlobalLayoutListener(this)
                animateFrontLayer(collapsedTranslation, adjustableViewPropertyAnimator)
            }
        })
    }


    fun toggle(translationAmount: Int = collapsedTranslation, animated: Boolean = true) {
        stateListeners.forEach { it.onPreToggle(expanded) }
        expanded = !expanded
        stateListeners.forEach { it.onToggle(expanded) }

        val desiredTranslation = if (expanded) DEFAULT_TRANSLATION else translationAmount
        if (animated) {
            animateFrontLayer(desiredTranslation)
        } else {
            forceFrontLayer(desiredTranslation)
        }
    }



    fun addAnimProgressListener(listener: BackDropAnimListener) {
        animListeners.add(listener)
    }

    fun addToggleListener(listener: BackdropStateListener) {
        stateListeners.add(listener)
    }

    fun removeAnimProgressListener(listener: BackDropAnimListener) {
        animListeners.remove(listener)
    }

    fun removeToggleListener(listener: BackdropStateListener) {
        stateListeners.remove(listener)
    }


    override fun onSaveInstanceState() = Bundle().apply {
        putParcelable(STATE_KEY, super.onSaveInstanceState())
        putBoolean(EXPANDED_KEY, expanded)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            super.onRestoreInstanceState(state.getParcelable(STATE_KEY))

            backLayer.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    backLayer.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    if (!state.getBoolean(EXPANDED_KEY)) {
//                        val coord = IntArray(2).apply { getLocationOnScreen(this) }
//                        toggle(max(adjustedTranslation - coord[Y_COORD_INDEX], 0), false)
                        toggle(max(collapsedTranslation, DEFAULT_TRANSLATION), false)
                    }
                }
            })
        } else {
            super.onRestoreInstanceState(state)
        }
    }
}