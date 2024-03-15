package com.example.controlii

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.EditText
import androidx.core.content.ContextCompat


class MyEditText : androidx.appcompat.widget.AppCompatEditText{
    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, defStyle: Int) : super(context, attributeSet, defStyle)


    override fun getTextSelectHandle(): Drawable? {
        val handle = ContextCompat.getDrawable(context, R.drawable.text_handle_middle)
        handle?.setTint(ContextCompat.getColor(context, R.color.color_primary))

        return handle ?: super.getTextSelectHandle()
    }

    override fun getTextSelectHandleLeft(): Drawable? {
        val handle = ContextCompat.getDrawable(context, R.drawable.text_handle_left)
        handle?.setTint(ContextCompat.getColor(context, R.color.color_primary))

        return handle ?: super.getTextSelectHandleLeft()
    }

    override fun getTextSelectHandleRight(): Drawable? {
        val handle = ContextCompat.getDrawable(context, R.drawable.text_handle_right)
        handle?.setTint(ContextCompat.getColor(context, R.color.color_primary))

        return handle ?: super.getTextSelectHandleRight()
    }





}