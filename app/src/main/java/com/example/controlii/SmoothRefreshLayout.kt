package com.example.controlii

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.Layout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.get


class SmoothRefreshLayout
@JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defineAttributeSet:Int = 0
) : ViewGroup(context, attributeSet, defineAttributeSet){




    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        var yOffset = 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val childHeight = child.measuredHeight

            if (i == 0) {
                child.layout(left, 100.dp, right, yOffset + childHeight)
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

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), 100.dp.toFloat(), Paint().apply { color = Color.RED })
        canvas.drawText("hello", width/2f, 40.dp.toFloat(), Paint().apply {
            color = Color.WHITE
            textSize = 100f
            textAlign = Paint.Align.CENTER
        })
        invalidate()
    }
}