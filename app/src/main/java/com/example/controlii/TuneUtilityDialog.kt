package com.example.controlii

import android.animation.ValueAnimator
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.WindowManager
import android.widget.ImageView
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.ContextCompat
import androidx.core.view.updatePaddingRelative
import com.example.controlii.databinding.DialogUtilityTuneBinding

class TuneUtilityDialog: AppCompatDialogFragment() {

    lateinit var onTuneChange: OnTuneChange

    lateinit var dut: DialogUtilityTuneBinding


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogLayout =  requireActivity().layoutInflater.inflate(R.layout.dialog_utility_tune, null)

        dut = DialogUtilityTuneBinding.inflate(layoutInflater)
        val view = dut.root

        val builder = requireActivity().let {
            AlertDialog.Builder(it,  R.style.TransparentDialogThemeForTuning)
                .setView(view)
        }
        
        dut.seekbar.progress = MyStoreInfo(requireContext()).readInt(SEEKBAR_VAL, DEFAULT_SEEKBAR_VAL)!!


        val countDownTimer = object: CountDownTimer(2000L, 1000L){
            override fun onTick(p0: Long) {

            }

            override fun onFinish() {
                dismiss()
            }

        }

        dut.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            val animatebar by lazy {animateSeekBar(dut.seekbar.height,dut.seekbar.height+5.dp)}
            override fun onProgressChanged(v: SeekBar?, value: Int, fromUser: Boolean) {
                val sunRotateVal = map(value, 0, 100, 0, 180)
                dut.sun.rotation = sunRotateVal

                MyStoreInfo(requireContext()).writeInt(SEEKBAR_VAL,value)
                onTuneChange.onTuneValChangeListener(value)
            }

            override fun onStartTrackingTouch(v: SeekBar?) {

                animatebar.start()
                countDownTimer.cancel()
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

                countDownTimer.start()
                animatebar.reverse()
            }

        })

        countDownTimer.start()

        return builder.create()
    }



    override fun onAttach(context: Context) {
        super.onAttach(context)
        onTuneChange = (context as OnTuneChange)

    }


    override fun onStart() {
        super.onStart()

        val layoutParams = WindowManager.LayoutParams().apply {
            copyFrom(dialog?.window?.attributes)

            windowAnimations = R.style.TransparentDialogThemeForTuning
            width = 50.dp
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.END
            dimAmount = 0f

            x = 20.dp
        }
        dialog?.window?.attributes = layoutParams


    }


    private fun animateSeekBar(from: Int, to: Int): ValueAnimator {
        val valueAnimator = ValueAnimator.ofInt(from, to)

        valueAnimator.duration = 100
        valueAnimator.addUpdateListener { animator ->
            val value = animator.animatedValue as Int

            val params = dut.seekbar.layoutParams
            params.height = value
            dut.seekbar.layoutParams = params
            dut.seekbar.requestLayout()
        }
        return valueAnimator
    }


    interface OnTuneChange {
        fun onTuneValChangeListener(tuneVal: Int)
    }

    companion object {
        const val SEEKBAR_VAL = "seekbarVal"
        const val DEFAULT_SEEKBAR_VAL = 50
    }



}


inline val Number.dp: Int
    get() {
        val density = Resources.getSystem().displayMetrics.density
        return when (this) {
            is Float -> (this * density).toInt()
            is Int -> (this * density).toInt()
            is Double -> (this.toFloat() * density).toInt()
            else -> throw IllegalArgumentException("Type not supported: ${this::class.simpleName}")
        }
    }
