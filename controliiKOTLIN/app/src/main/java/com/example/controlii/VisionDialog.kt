package com.example.controlii

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.coordinatorlayout.widget.CoordinatorLayout

class VisionDialog: AppCompatDialogFragment(){

    lateinit var editUrl: MyEditText
    lateinit var closeDialog: ImageButton
    lateinit var searchAction: SearchAction


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialogLayout =requireActivity().layoutInflater.inflate(R.layout.vision_dialog, null)

        val builder = requireActivity().let {
            AlertDialog.Builder(it, R.style.TransparentDialogTheme)
                .setView(dialogLayout)
        }

        editUrl = dialogLayout.findViewById(R.id.editUrl)
        editUrl.requestFocus()

        editUrl.setOnEditorActionListener { textView, id, keyEvent ->
            if(id == EditorInfo.IME_ACTION_SEARCH) {
                searchAction.OnSearchActionListener(textView.text.toString())
                dismiss()
            }
            false
        }

        closeDialog = dialogLayout.findViewById(R.id.sendButton)
        closeDialog.setOnClickListener {
            dismiss()
        }

        return builder.create()
    }

    override fun onStart() {
        super.onStart()
        dialog?.setCanceledOnTouchOutside(false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            searchAction = context as SearchAction
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + "must implementSearchAction")
        }

    }


    interface SearchAction {
        fun OnSearchActionListener(url: String)
    }
}




