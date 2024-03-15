package com.example.controlii


import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.webkit.WebView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.recyclerview.widget.RecyclerView

import kotlin.math.sin

class CustomRefreshLayout @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttribute: Int = 0
): LinearLayout(context, attributeSet, defStyleAttribute) {


    private var refreshLayout: View
    private var initTouchY = 0f
    private var deltaY = 0

    lateinit var refreshIcon: ImageView
    lateinit var refreshText: TextView
    lateinit var dynamicHolder: LinearLayout

    private var fixedHeight = 0

    private var isRefreshing = false

    private var onRefreshListener: OnRefreshListener? = null

    private val rotateAnimation = RotateAnimation(
        0f, 360f,
        Animation.RELATIVE_TO_SELF, 0.5f,
        Animation.RELATIVE_TO_SELF, 0.5f
    )


    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.refresh_layout, this, true)

        refreshLayout = getChildAt(0).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, fixedHeight)
            requestLayout()
        }
        fixedHeight = 64.dp

        refreshIcon = refreshLayout.findViewById(R.id.icon)
        refreshIcon.alpha = 0f
        refreshText = refreshLayout.findViewById(R.id.text)
        refreshText.alpha = 0f
        dynamicHolder = refreshLayout.findViewById(R.id.dynamicHolder)



    }
    
    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        when(event?.action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                return false
            }
            MotionEvent.ACTION_DOWN -> {
                initTouchY = event.y
                return false
            }
            MotionEvent.ACTION_MOVE -> {

                var instantY = event.y - initTouchY

                when(val childView = getChildAt(1)) {
                    is RecyclerView -> {
                        if(!childView.canScrollVertically(-1)) {
                            if(instantY > 0 && !isRefreshing) {
                                return true
                            }
                        }
                    }
                    is WebView -> {
                        if(childView.scrollY == 0) {
                            if(instantY > 0 && !isRefreshing && isHidden()) {
                                return true
                            }
                        }
                    }
                }





            }
            else -> {
                return false
            }
        }

        return false

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when(event?.action) {
            MotionEvent.ACTION_MOVE -> {
                deltaY =(event.y - initTouchY).toInt()
                if(!isRefreshing) {

                    val validDelta = deltaY.coerceIn(0, height)

                    if(validDelta >= fixedHeight -30.dp) {
                        val mappedScale = map(validDelta, fixedHeight - 30.dp, fixedHeight, 0, 1)
                        refreshText.alpha = mappedScale
                        refreshIcon.alpha = mappedScale
                    }
                    if(validDelta <= fixedHeight){


                        val mappedScale = map(validDelta, 0, fixedHeight, 0, 1)


                        dynamicHolder.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, fixedHeight).apply {
                            requestLayout()
                        }
                        refreshIcon.apply {
                            scaleX = mappedScale
                            scaleY = mappedScale
                            pivotX = refreshIcon.width/2f
                            pivotY = refreshIcon.height.toFloat()
                        }

                        requestLayout()

                        refreshText.text = "Pull down to refresh"

                    } else {
                        if(refreshLayout.height >= fixedHeight + 2.dp) {
                            refreshText.text = "Release to refresh"
                        }
                        dynamicHolder.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                        refreshIcon.apply {
                            scaleX = 1f
                            scaleY = 1f
                        }



                    }

                    refreshLayout.layoutParams.height = deltaY.coerceIn(0, height)
                    refreshLayout.requestLayout()



                }


                return false
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if(refreshLayout.height >= fixedHeight + 2.dp) {
                    val animator1 = ObjectAnimator.ofInt(refreshLayout.height, fixedHeight)
                    animator1.addUpdateListener {
                        refreshLayout.layoutParams.height = animator1.animatedValue as Int
                        refreshLayout.requestLayout()
                    }
                    animator1.doOnEnd {
                        dynamicHolder.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, fixedHeight)
                        isRefreshing = true
                        onRefreshListener?.onRefresh()
                        refreshText.text = "Refreshing..."
                        startSpin(true)
                    }
                    animator1.duration = 300
                    animator1.start()


                }
                else {
                    refreshText.alpha = 0f
                    hideAnimation(!isRefreshing)
                }

                if(refreshLayout.height <= fixedHeight) {
                    dynamicHolder.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, fixedHeight).apply {
                        requestLayout()
                    }
                } else {
                    dynamicHolder.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply {
                        requestLayout()
                    }
                }

                return false

            }

        }
        return false
    }




    private fun hideAnimation(hide: Boolean) {
        val animator = ObjectAnimator.ofInt(refreshLayout.height, 0)
        animator.addUpdateListener {
            refreshLayout.layoutParams.height = animator.animatedValue as Int
            refreshLayout.requestLayout()

        }
        animator.doOnEnd {
            isRefreshing = false
            refreshText.alpha = 0f
            refreshIcon.alpha = 0f
            startSpin(false)
        }

        animator.duration = 300
        if(hide) {
            dynamicHolder.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, fixedHeight).apply {
                requestLayout()
            }
            animator.start()
        }
    }
    private fun appearAnimation() {
        val animator = ObjectAnimator.ofInt(0, fixedHeight)
        animator.addUpdateListener {
            refreshLayout.layoutParams.height = animator.animatedValue as Int
            refreshLayout.requestLayout()

            if(animator.animatedValue as Int >= fixedHeight -30.dp) {
                val mappedAnimator = map(animator.animatedValue as Int, 0, fixedHeight, 0, 1)
                refreshIcon.apply {
                    scaleX = mappedAnimator
                    scaleY = mappedAnimator
                    pivotX = refreshIcon.width / 2f
                    pivotY = refreshIcon.height.toFloat()
                    alpha = mappedAnimator
                }
                refreshText.alpha = mappedAnimator
            }


        }
        animator.doOnEnd {

            startSpin(true)
            isRefreshing = true
        }
        animator.duration = 300

        dynamicHolder.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, fixedHeight).apply {
            requestLayout()
        }


        if(!isRefreshing) {
            animator.start()

        }
    }

    private fun rotateAnim(start: Boolean) {


        rotateAnimation.duration = 1000
        rotateAnimation.repeatCount = Animation.INFINITE
        rotateAnimation.interpolator = LinearInterpolator()

        if (start) {

            refreshIcon.startAnimation(rotateAnimation)

        } else {
            rotateAnimation.cancel()
        }

    }

    private fun reInit() {
        refreshLayout = getChildAt(0).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, fixedHeight)
            requestLayout()
        }
        fixedHeight = 64.dp
    }

    private fun isHidden() = refreshLayout.height == 0

    private fun startSpin(start: Boolean) {
        if(start) {
            refreshIcon.setImageResource(R.drawable.refresh_icon_animated)
            val animatedVectorDrawable = refreshIcon.drawable as AnimatedVectorDrawable

            animatedVectorDrawable.registerAnimationCallback(object : Animatable2.AnimationCallback() {
                override fun onAnimationEnd(drawable: Drawable?) {
                    if(isRefreshing) {
                        animatedVectorDrawable.start()
                    } else {
                        animatedVectorDrawable.reset()
                    }
                }
            })

            animatedVectorDrawable.start()

        } else {
            refreshIcon.setImageResource(R.drawable.refresh_expand)
        }
    }


    var refreshing = false
        set(value) {
            if(!value) {

                hideAnimation(true)
                startSpin(false)


            }
            if(value) {

                appearAnimation()
                onRefreshListener?.onRefresh()

            }
            field = value
        }



    fun setOnRefreshListener(onRefreshListener: CustomRefreshLayout.OnRefreshListener) {
        this.onRefreshListener = onRefreshListener
    }

    interface OnRefreshListener {
        fun onRefresh()
    }



}








