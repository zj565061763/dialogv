package com.sd.lib.dialog.impl

import android.app.Activity

internal object FDialogHolder {
    private val dialogHolder = mutableMapOf<Activity, MutableCollection<FDialog>>()

    @JvmStatic
    fun addDialog(dialog: FDialog) {
        val activity = dialog.ownerActivity

        var holder = dialogHolder[activity]
        if (holder == null) {
            holder = mutableListOf()
            dialogHolder[activity] = holder
        }

        holder.lastOrNull()?.notifyCover()
        holder.add(dialog)
    }

    @JvmStatic
    fun removeDialog(dialog: FDialog) {
        val activity = dialog.ownerActivity
        val holder = dialogHolder[activity] ?: return

        val remove = holder.remove(dialog)
        if (remove) {
            holder.lastOrNull()?.notifyCoverRemove()
        }

        if (holder.isEmpty()) {
            dialogHolder.remove(activity)
        }
    }

    @JvmStatic
    fun get(activity: Activity): List<FDialog>? {
        val holder = dialogHolder[activity] ?: return null
        return holder.toMutableList()
    }

    @JvmStatic
    fun getLast(activity: Activity): FDialog? {
        val holder = dialogHolder[activity] ?: return null
        return holder.lastOrNull()
    }

    @JvmStatic
    fun remove(activity: Activity) {
        dialogHolder.remove(activity)
    }
}