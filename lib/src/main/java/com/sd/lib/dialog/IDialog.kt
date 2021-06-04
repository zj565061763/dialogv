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
     * 重力属性[android.view.Gravity]
     */
    var gravity: Int

    /**
     * 窗口显示对象
     */
    var display: Display

    /**
     * 返回窗口的内容view
     */
    fun getContentView(): View?

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
     * 设置背景模糊
     */
    fun setBackgroundDim(dim: Boolean)

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
     * 设置动画时长
     */
    fun setAnimatorDuration(duration: Long)

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
     * 显示窗口
     */
    fun show()

    /**
     * 关闭窗口
     */
    fun dismiss()

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