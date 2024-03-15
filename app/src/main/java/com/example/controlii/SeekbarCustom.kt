package com.example.controlii

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ModuleInfo
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.SeekBar



class SeekbarCustom : androidx.appcompat.widget.AppCompatSeekBar {
    constructor(context: Context) : super(context) {
        initialize()
    }
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        initialize()
    }
    constructor(context: Context, attributeSet: AttributeSet, defStyle: Int) : super(context, attributeSet, defStyle) {
        initialize()
    }

    @SuppressLint("ClickableViewAccessibility")
    fun initialize() {
        setOnTouchListener { view, motionEvent ->
            true
        }
    }

    private val thumbBounds = Rect()
    var initialY = 0


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (thumb == null) {
            // If thumb is null, calculate the bounds based on the progress position
            val thumbWidth = height / 2 // Adjust thumb width as per your requirement
            val progressWidth = width - paddingLeft - paddingRight
            val progressX = paddingLeft + (progressWidth * progress / max)
            thumbBounds.set(
                (progressX - thumbWidth / 2),
                paddingTop,
                (progressX + thumbWidth / 2),
                height - paddingBottom
            )
        } else {
            thumb.copyBounds(thumbBounds)
        }

        when (event?.action) {

            MotionEvent.ACTION_DOWN -> {
                initialY = event.y.toInt()
                if (event.x >= thumbBounds.left
                    && event.x <= thumbBounds.right
                    && event.y >= thumbBounds.top
                    && event.y <= thumbBounds.bottom
                ) {
                    super.onTouchEvent(event)
                } else {
                    return false
                }
            }
            MotionEvent.ACTION_MOVE -> {
                super.onTouchEvent(event)
                val dy = event.y - initialY

                progress =(progress + dy).toInt()
            }
            MotionEvent.ACTION_UP -> return false
        }

        return true
    }
}