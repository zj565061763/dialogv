package com.sd.lib.dialog

import android.app.Activity
import android.content.Context
import android.view.View
import com.sd.lib.dialog.animator.AnimatorCreator

interface IDialog {
    /**
     * 调试模式，内部会输出日志，日志tag：IDialog
     */
    var isDebug: Boolean

    /**
     * 返回Context对象
     */
    val context: Context

    /**
     * 返回构造方法传入的Activity
     */
    val ownerActivity: Activity

    /**
     * [AnimatorCreator]
     */
    var animatorCreator: AnimatorCreator?

    /**
     * 动画时长
     */
    var animatorDuration: Long

    /**
     * 重力属性，默认[android.view.Gravity.CENTER]
     */
    var gravity: Int

    /**
     * 是否半透明背景，默认true
     */
    var isBackgroundDim: Boolean

    /**
     * 窗口显示对象
     */
    var display: Display

    /**
     * 设置上下左右间距，如果某个方向的值小于0，则该方向的padding保持原有的值不变
     */
    fun setPadding(left: Int, top: Int, right: Int, bottom: Int)

    /**
     * 左边边距
     */
    val paddingLeft: Int

    /**
     * 顶部边距
     */
    val paddingTop: Int

    /**
     * 右边边距
     */
    val paddingRight: Int

    /**
     * 底部边距
     */
    val paddingBottom: Int

    /**
     * 窗口是否正在显示
     */
    val isShowing: Boolean

    /**
     * 窗口的内容view
     */
    val contentView: View?

    /**
     * 设置窗口的内容view布局id
     */
    fun setContentView(layoutId: Int)

    /**
     * 设置窗口的内容view
     */
    fun setContentView(view: View?)

    /**
     * 根据id查找view
     */
    fun <T : View> findViewById(id: Int): T?

    /**
     * 设置窗口是否可以取消，默认true
     */
    fun setCancelable(cancel: Boolean)

    /**
     * 设置触摸到非内容view区域是否关闭窗口，默认true
     */
    fun setCanceledOnTouchOutside(cancel: Boolean)

    /**
     * 设置窗口关闭监听
     */
    fun setOnDismissListener(listener: OnDismissListener?)

    /**
     * 设置窗口显示监听
     */
    fun setOnShowListener(listener: OnShowListener?)

    /**
     * 设置窗口取消监听
     */
    fun setOnCancelListener(listener: OnCancelListener?)

    /**
     * 显示窗口
     */
    fun show()

    /**
     * 关闭窗口
     */
    fun dismiss()

    /**
     * 取消
     */
    fun cancel()

    /**
     * 延迟多久后关闭dialog
     *
     * @param delay （毫秒）
     */
    fun startDismissRunnable(delay: Long)

    /**
     * 停止延迟关闭任务
     */
    fun stopDismissRunnable()

    /**
     * 返回[ITargetDialog]对象
     */
    fun target(): ITargetDialog

    /**
     * 关闭监听
     */
    fun interface OnDismissListener {
        /**
         * 消失后回调
         */
        fun onDismiss(dialog: IDialog)
    }

    /**
     * 显示监听
     */
    fun interface OnShowListener {
        /**
         * 显示后回调
         */
        fun onShow(dialog: IDialog)
    }

    /**
     * 取消监听
     */
    fun interface OnCancelListener {
        /**
         * 取消回调
         */
        fun onCancel(dialog: IDialog)
    }

    interface Display {
        /**
         * 显示窗口
         */
        fun showDialog(view: View)

        /**
         * 关闭窗口
         */
        fun dismissDialog(view: View)
    }
}