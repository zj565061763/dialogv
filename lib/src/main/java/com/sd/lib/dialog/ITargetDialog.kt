package com.sd.lib.dialog

import android.view.View
import com.sd.lib.vtrack.tracker.ViewTracker

interface ITargetDialog {
    /**
     * x方向偏移量，大于0-向右；小于0-向左
     */
    fun setMarginX(margin: Int): ITargetDialog

    /**
     * y方向偏移量，大于0-向下；小于0-向上
     */
    fun setMarginY(margin: Int): ITargetDialog

    /**
     * 设置是否偏移背景，默认true
     */
    fun setTranslateBackground(translate: Boolean): ITargetDialog

    /**
     * 设置目标View
     */
    fun setTarget(target: View?): ITargetDialog

    /**
     * 设置目标位置信息
     */
    fun setTargetLocationInfo(locationInfo: ViewTracker.LocationInfo?): ITargetDialog

    /**
     * 设置窗口显示在目标的[position]位置，默认[Position.BottomOutside]
     */
    fun setPosition(position: Position): ITargetDialog

    /**
     * 刷新位置
     */
    fun update()

    /**
     * 显示窗口显
     */
    fun show()

    enum class Position {
        /** 在target左边外侧 */
        LeftOutside,

        /** 在target的左边外侧靠顶部对齐 */
        LeftOutsideTop,

        /** 在target的左边外侧上下居中 */
        LeftOutsideCenter,

        /** 在target的左边外侧靠底部对齐 */
        LeftOutsideBottom,


        /** 在target的顶部外侧 */
        TopOutside,

        /** 在target的顶部外侧靠左对齐 */
        TopOutsideLeft,

        /** 在target的顶部外侧左右居中 */
        TopOutsideCenter,

        /** 在target的顶部外侧靠右对齐 */
        TopOutsideRight,


        /** 在target的右边外侧 */
        RightOutside,

        /** 在target的右边外侧靠顶部对齐 */
        RightOutsideTop,

        /** 在target的右边外侧上下居中 */
        RightOutsideCenter,

        /** 在target的右边外侧靠底部对齐 */
        RightOutsideBottom,


        /** 在target的底部外侧 */
        BottomOutside,

        /** 在target的底部外侧靠左对齐 */
        BottomOutsideLeft,

        /** 在target的底部外侧左右居中 */
        BottomOutsideCenter,

        /** 在target的底部外侧靠右对齐 */
        BottomOutsideRight
    }
}