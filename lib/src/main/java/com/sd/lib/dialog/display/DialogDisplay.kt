package com.sd.lib.dialog.display

import android.app.Dialog
import android.view.View
import android.view.ViewGroup
import com.sd.lib.dialog.IDialog
import com.sd.lib.dialog.R

class DialogDisplay : IDialog.Display {
    private var _dialog: Dialog? = null

    override fun showDialog(view: View) {
        val dialog = Dialog(view.context, R.style.lib_dialog_default).apply {
            _dialog = this
            setContentView(
                view,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            )
            setCanceledOnTouchOutside(false)
            setCancelable(false)
        }

        try {
            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun dismissDialog(view: View) {
        try {
            _dialog?.dismiss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}