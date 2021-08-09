package com.sd.demo.dialog

import android.app.Activity
import android.view.View
import android.widget.Toast
import com.sd.lib.dialog.impl.FDialog

class PopDialog(activity: Activity) : FDialog(activity) {
    init {
        setPadding(0, 0, 0, 0)
        setContentView(R.layout.dialog_pop)
        setBackgroundDim(false)

        findViewById<View>(R.id.btn)!!.setOnClickListener {
            Toast.makeText(context, "clicked", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }
}