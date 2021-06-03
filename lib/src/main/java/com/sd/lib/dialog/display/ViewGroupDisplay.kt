package com.sd.lib.dialog.display

import android.view.View
import android.view.ViewGroup
import com.sd.lib.dialog.IDialog

class ViewGroupDisplay : IDialog.Display {
    private val _viewGroup: ViewGroup

    constructor(viewGroup: ViewGroup) {
        _viewGroup = viewGroup
    }

    override fun showDialog(view: View) {
        _viewGroup.addView(
            view,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
    }

    override fun dismissDialog(view: View) {
        _viewGroup.removeView(view)
    }
}