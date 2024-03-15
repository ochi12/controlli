package com.example.controlii

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.SeekBar
import com.example.controlii.databinding.DialogUtilityTuneBinding


class BrightnessSlider(val context: Context, val brightnessSliderInterface: BrightnessSliderInterface) {

    private val speed = 400L
    private val _startOffset = 200L

    private var view: View = LayoutInflater.from(context).inflate(R.layout.dialog_utility_tune, null)
    private var binding: DialogUtilityTuneBinding = DialogUtilityTuneBinding.bind(view)

    val handler = Handler(Looper.getMainLooper())

    val animatebar by lazy {animateSeekBar(binding.seekbar.height,binding.seekbar.height+5.dp)}


    companion object {
        const val SEEKBAR_VAL = "seekbarVal"
        const val DEFAULT_SEEKBAR_VAL = 50
    }

    @SuppressLint("ClickableViewAccessibility")
    fun show() {

        binding.seekbar.progress = MyStoreInfo(context).readInt(
            SEEKBAR_VAL,
            DEFAULT_SEEKBAR_VAL
        )!!


        // Create an overlay view
        val overlayView = View(context)
        overlayView.setBackgroundColor(Color.TRANSPARENT)

        // Set the overlay view to match the parent's size
        val overlayLayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )


        val layoutParams = FrameLayout.LayoutParams(
            120.dp,
            300.dp,
            Gravity.END or Gravity.CENTER_VERTICAL
        )
        val rootView = (context as? android.app.Activity)
            ?.window
            ?.decorView
            ?.findViewById(android.R.id.content) as ViewGroup

        rootView.apply {
            addView(overlayView, overlayLayoutParams)
            addView(view, layoutParams)
            binding.holder.startAnimation(animShow())
        }




        val countDownTimer = object : CountDownTimer(1700, 1000) {
            override fun onTick(p0: Long) {

            }

            override fun onFinish() {
                rootView.apply {

                    handler.postDelayed({
                        removeView(view)
                        rootView.removeView(overlayView)
                    },_startOffset)
                    binding.holder.startAnimation(animHide())
                }
            }

        }
        overlayView.setOnClickListener {
            handler.postDelayed({
                rootView.removeView(view)
                rootView.removeView(overlayView)
            }, _startOffset)
            countDownTimer.cancel()

            binding.holder.startAnimation(animHide())
        }


        countDownTimer.start()

//        binding.seekbar.setOnTouchListener { _, motionEvent ->
//            when(motionEvent.action) {
//                MotionEvent.ACTION_DOWN -> {
//
//                }
//                MotionEvent.ACTION_UP -> {
//                    countDownTimer.start()
//                    animatebar.reverse()
//                }
//            }
//            false
//        }
        binding.seekbar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, value: Int, p2: Boolean) {
                brightnessSliderInterface.onBrightnessSliderChange(value)
                val rotation = map(value, 0, 100, 0, 180)

                binding.sun.rotation = rotation

                MyStoreInfo(context).writeInt(SEEKBAR_VAL,value)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                animatebar.start()
                countDownTimer.cancel()
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                countDownTimer.start()
                animatebar.reverse()
            }

        })
    }

    private fun animateSeekBar(from: Int, to: Int): ValueAnimator {
        val valueAnimator = ValueAnimator.ofInt(from, to)

        valueAnimator.duration = 100
        valueAnimator.addUpdateListener { animator ->
            val value = animator.animatedValue as Int

            val params = binding.seekbar.layoutParams
            params.height = value
            binding.seekbar.layoutParams = params
            binding.seekbar.requestLayout()
        }
        return valueAnimator
    }


    private fun animShow(): AnimationSet {
        val fadeAnim = AlphaAnimation(0f, 1f).apply {
            duration = speed
        }
        val scaleAnim = ScaleAnimation(
            0.9f,
            1f,
            0.9f,
            1f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        ).apply {
            duration = speed
            startOffset = _startOffset
        }

        val transAnim = TranslateAnimation(
            Animation.RELATIVE_TO_SELF,
            2f,
            Animation.RELATIVE_TO_SELF,
            0f,
            Animation.RELATIVE_TO_SELF,
            0f,
            Animation.RELATIVE_TO_SELF,
            0f
        ).apply {
            duration = speed
        }

        return AnimationSet(false).apply {
            addAnimation(fadeAnim)
            addAnimation(scaleAnim)
            addAnimation(transAnim)
        }

    }

    private fun animHide(): AnimationSet {
        val fadeAnim = AlphaAnimation(1f, 0f).apply {
            duration = speed
            startOffset = _startOffset
        }
        val scaleAnim = ScaleAnimation(
            1f,
            0.9f,
            1f,
            0.9f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        ).apply {
            duration = speed
        }

        val transAnim = TranslateAnimation(
            Animation.RELATIVE_TO_SELF,
            0f,
            Animation.RELATIVE_TO_SELF,
            2f,
            Animation.RELATIVE_TO_SELF,
            0f,
            Animation.RELATIVE_TO_SELF,
            0f
        ).apply {
            duration = speed
            startOffset = _startOffset
        }

        return AnimationSet(false).apply {
            addAnimation(fadeAnim)
            addAnimation(scaleAnim)
            addAnimation(transAnim)
        }

    }


    interface BrightnessSliderInterface {
        fun onBrightnessSliderChange(value: Int)
    }
}