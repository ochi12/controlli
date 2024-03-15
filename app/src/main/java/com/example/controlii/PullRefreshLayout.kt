package com.example.controlii

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.view.NestedScrollingChild
import androidx.core.view.NestedScrollingChild2
import androidx.core.view.NestedScrollingChild3
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import java.time.format.DecimalStyle

class PullRefreshLayout @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defineStyleAttribute: Int = 0
): ViewGroup(context, attributeSet, defineStyleAttribute),NestedScrollingChild, NestedScrollingChild2, NestedScrollingChild3 {


    private var helper: NestedScrollingChildHelper? = null

    private var onRefreshListener: OnRefreshListener? = null
    //view related
    private var refreshView: View
    private var fixedHeight = 0

    private var refreshText: TextView
    private var refreshIcon: ImageView
    private var dynamicHolder: LinearLayout

    private var mIsRefreshing = false

    //touch event related
    private var initialTouchY = 0f
    private var deltaY = 0

    init {

        LayoutInflater.from(context).inflate(R.layout.refresh_layout, this, true)

        refreshView = getChildAt(0).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, fixedHeight)
            requestLayout()
        }
        fixedHeight = 65.dp

        refreshText = refreshView.findViewById<TextView?>(R.id.text).apply {
            alpha = 0f
        }
        refreshIcon = refreshView.findViewById(R.id.icon)
        dynamicHolder = refreshView.findViewById(R.id.dynamicHolder)



    }




    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        var yOffset = 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val childHeight = child.measuredHeight

            if (i == 0) {
                child.layout(left, yOffset, right, yOffset + childHeight)
                yOffset += childHeight
            } else {
                child.layout(left, yOffset, right, bottom)

            }
        }
    }



    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        val childCount = childCount
        var totalHeight = 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            totalHeight += child.measuredHeight
        }

        setMeasuredDimension(widthSize, totalHeight)
    }

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {

        getChildAt(1).dispatchTouchEvent(event)
        when(event?.actionMasked) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {

                return false
            }
            MotionEvent.ACTION_DOWN -> {
                initialTouchY = event.y

                return false
            }
            MotionEvent.ACTION_MOVE -> {
                val instantY = event.y - initialTouchY
                when (val parentChildView = getChildAt(1)) {
                    is RecyclerView -> {
                        if(!parentChildView.canScrollVertically(-1)) {
                            if(instantY > 0 && !mIsRefreshing) {

                                return true
                            }
                        } else {
                            return false
                        }

                    }
                    is WebView -> {
                        if(parentChildView.scrollY == 0) {
                            if(instantY > 0 && !mIsRefreshing) {
                                return true
                            }
                        }
                    }
                    else -> {
                        if(instantY > 0 && !mIsRefreshing) {
                            return true
                        }
                    }
                }
            }
            else -> return false
        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val r: SwipeRefreshLayout


        when(event?.actionMasked) {

            MotionEvent.ACTION_MOVE -> {


                deltaY = (event.y - initialTouchY).toInt()


                if(!mIsRefreshing) {

                    refreshIcon.setImageResource(R.drawable.refresh_expand)

                    val validDelta = deltaY.coerceIn(0, height)

                    if(validDelta in fixedHeight - 20.dp .. fixedHeight) {
                        refreshText.alpha = map(
                            validDelta,
                            fixedHeight - 20.dp,
                            fixedHeight,
                            0,
                            1
                        )
                    }

                    if(validDelta <= fixedHeight) {
                        val mappedScale = map(validDelta, 0, fixedHeight, 0, 1)
                        refreshIcon.apply {
                            scaleX = mappedScale
                            scaleY = mappedScale
                            pivotX = refreshIcon.width/2f
                            pivotY = refreshIcon.height.toFloat()
                            alpha = mappedScale
                        }
                        dynamicHolder.layoutParams.height = fixedHeight
                        dynamicHolder.requestLayout()
                    } else {
                        dynamicHolder.layoutParams.height = LayoutParams.MATCH_PARENT
                        dynamicHolder.requestLayout()
                    }

                    refreshView.layoutParams.height = deltaY.coerceIn(0, height)
                    refreshView.requestLayout()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                hideRefreshView()
            }
        }
        return super.onTouchEvent(event)
    }

    private fun hideRefreshView() {



        var endPosition = 0
        if(refreshView.height >= fixedHeight + 2.dp) {
            endPosition = fixedHeight
        } else {
            endPosition = 0
        }
        val hideAnimator = ValueAnimator.ofInt(refreshView.height,endPosition)
        hideAnimator.apply {
            addUpdateListener {
                refreshView.apply {
                    layoutParams.height = animatedValue as Int
                    requestLayout()
                }
            }
            duration = 300
            start()

            doOnEnd {
                dynamicHolder.layoutParams.height = fixedHeight
                dynamicHolder.requestLayout()

                mIsRefreshing = endPosition != 0
                if(endPosition != 0) {
                    if(onRefreshListener != null) {
                        onRefreshListener?.onRefresh()
                    }
                    refreshIcon.setImageResource(R.drawable.refresh_icon)
                }
                removeView(refreshView)
                requestLayout()
            }
        }
    }

    fun setOnRefreshListener(onRefreshListener: OnRefreshListener) {
        this.onRefreshListener = onRefreshListener
    }

    interface OnRefreshListener {
        fun onRefresh()
    }


    // Nested Scrolling Child 3
    override fun startNestedScroll(axes: Int, type: Int): Boolean {
        return helper!!.startNestedScroll(axes, type)
    }

    override fun stopNestedScroll(type: Int) {
        helper!!.stopNestedScroll(type)
    }

    override fun hasNestedScrollingParent(type: Int): Boolean {
        return helper!!.hasNestedScrollingParent(type)
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?,
        type: Int,
        consumed: IntArray
    ) {
        helper!!.dispatchNestedScroll(
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            offsetInWindow,
            type,
            consumed
        )
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?,
        type: Int
    ): Boolean {
        return helper!!.dispatchNestedScroll(
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            offsetInWindow,
            type
        )
    }

    override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?,
        type: Int
    ): Boolean {
        return helper!!.dispatchNestedPreScroll(
            dy,
            dy,
            consumed,
            offsetInWindow,
            type
        )
    }





}

