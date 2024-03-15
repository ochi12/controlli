package com.example.controlii

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import com.example.controlii.databinding.MToastLayoutBinding
import java.lang.ref.WeakReference
import java.util.LinkedList

typealias MyToast = Toast
object Toast {
    private const val speed = 300L
    private const val delayOffset = 150L

    private var context: WeakReference<Context>? = null
    private var msg: WeakReference<CharSequence>? = null
    private var duration = 0L

    private var toastQue : LinkedList<CharSequence> = LinkedList()


    private var isToastShown = false


    fun makeText(context: Context, msg: CharSequence, duration: Long): Toast{
        this.context = WeakReference(context)
        this.msg = WeakReference(msg)
        this.duration = duration
        toastQue.add(msg)
        return this
    }

    fun show(){
        if(!isToastShown) showToastInQue()
    }

    private fun showToastInQue() {

        if(toastQue.isNotEmpty()) {
            val currentMesssage = toastQue.poll()

            val layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            )


            val strongContext = context?.get()
            val rootView =
                (strongContext as? android.app.Activity)
                    ?.window
                    ?.decorView
                    ?.findViewById(android.R.id.content) as ViewGroup


            val view =
                LayoutInflater.from(strongContext).inflate(R.layout.m_toast_layout, null)
            val binding = MToastLayoutBinding.bind(view)
            binding.toastMessage.text = currentMesssage



            rootView.apply {

                addView(view, layoutParams)
                binding.toastBackground.startAnimation(animIn())
                isToastShown = true

                postDelayed({
                    binding.toastBackground.startAnimation(animOut())
                }, duration)
                postDelayed({
                    removeView(view)
                    isToastShown = false
                    showToastInQue()
                }, duration + speed)

            }
        }



    }
    private  fun animIn() : AnimationSet {
        val fade = AlphaAnimation(0f, 1f).apply {
            duration = speed
        }
        val scale = ScaleAnimation(
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
            startOffset = delayOffset
        }

        val translate = TranslateAnimation(
            Animation.RELATIVE_TO_SELF,
            0f,
            Animation.RELATIVE_TO_SELF,
            0f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0f
        ).apply {
            duration = speed
        }

        return AnimationSet(false).apply {
            addAnimation(fade)
            addAnimation(scale)
            addAnimation(translate)
        }

    }

    private fun animOut() : AnimationSet {
        val fade = AlphaAnimation(1f, 0f).apply {
            duration = speed
            startOffset = delayOffset
        }
        val scale = ScaleAnimation(
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

        val translate = TranslateAnimation(
            Animation.RELATIVE_TO_SELF,
            0f,
            Animation.RELATIVE_TO_SELF,
            0f,
            Animation.RELATIVE_TO_SELF,
            0f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        ).apply {
            duration = speed
            startOffset = delayOffset
        }


        return AnimationSet(false).apply {
            addAnimation(fade)
            addAnimation(scale)
            addAnimation(translate)
        }

    }



}