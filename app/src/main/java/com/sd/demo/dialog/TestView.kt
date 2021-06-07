package com.sd.demo.dialog

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout

class TestView : FrameLayout {
    val root by lazy {
        LinearLayout(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        addView(root)
        root.addView(EditText(context).apply {
            this.layoutParams = ViewGroup.LayoutParams(300, 100)
        })
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.dispatch(this, keyDispatcherState, this)) {
            return true
        }

        return super.dispatchKeyEvent(event).also {
            Log.i(TAG, "dispatchKeyEvent ${it} ${this@TestView}")
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return super.onKeyDown(keyCode, event).also {
            event.startTracking()
            Log.i(TAG, "onKeyDown ${it} ${this@TestView}")
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        return super.onKeyUp(keyCode, event).also {
            Log.i(TAG, "onKeyUp ${it} ${event.isTracking} ${this@TestView}")
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isFocusable = true
        requestFocus()
    }

    companion object {
        val TAG = TestView::class.java.simpleName
    }
}