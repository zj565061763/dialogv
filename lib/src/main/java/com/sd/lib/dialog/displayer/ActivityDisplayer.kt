package com.sd.lib.dialog.displayer

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import com.sd.lib.dialog.IDialog

class ActivityDisplayer : IDialog.Displayer {
    override fun showDialog(view: View) {
        val context = view.context
        if (context is Activity) {
            val container = context.findViewById<ViewGroup>(android.R.id.content)
            container.addView(view)
        }
    }

    override fun dismissDialog(view: View) {
        val context = view.context
        if (context is Activity) {
            val container = context.findViewById<ViewGroup>(android.R.id.content)
            container.removeView(view)
        }
    }
}