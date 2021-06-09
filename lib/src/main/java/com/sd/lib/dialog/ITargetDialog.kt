package com.sd.lib.dialog

import android.view.View

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
     * 设置是否偏移背景，默认false
     */
    fun setTranslateBackground(translate: Boolean): ITargetDialog

    /**
     * 显示在目标view的某个位置
     *
     * @param target   目标view
     * @param position 显示的位置[Position]
     */
    fun show(target: View?, position: Position)

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