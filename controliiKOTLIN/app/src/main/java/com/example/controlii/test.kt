
package com.example.controlii

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.TouchDelegate
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.core.view.setPadding
import com.google.android.material.slider.Slider
import kotlin.math.abs
import kotlin.properties.Delegates

class test : AppCompatActivity() {
    lateinit var seekBar: SeekBar
    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingInflatedId", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_utility_tune)

        val sun = findViewById<ImageView>(R.id.sun)
        seekBar = findViewById(R.id.seekbar)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(v: SeekBar?, value: Int, fromUser: Boolean) {
                val sunRotateVal = map(value, 0, 100, 0, 180)
                sun.rotation = sunRotateVal
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }

        })
    }
}