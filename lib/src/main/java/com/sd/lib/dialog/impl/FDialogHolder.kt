package com.sd.lib.dialog.impl

import android.app.Activity
import java.util.*

internal object FDialogHolder {
    private val mapActivityDialog: MutableMap<Activity, MutableCollection<FDialog>> = HashMap()

    @JvmStatic
    fun addDialog(dialog: FDialog) {
        val activity = dialog.ownerActivity
        var holder = mapActivityDialog[activity]
        if (holder == null) {
            holder = mutableListOf()
            mapActivityDialog[activity] = holder
        }

        holder.lastOrNull()?.notifyCover()
        holder.add(dialog)
    }

    @JvmStatic
    fun removeDialog(dialog: FDialog) {
        val activity = dialog.ownerActivity
        val holder = mapActivityDialog[activity] ?: return

        val remove = holder.remove(dialog)
        if (remove) {
            holder.lastOrNull()?.notifyCoverRemove()
        }

        if (holder.isEmpty()) {
            mapActivityDialog.remove(activity)
        }
    }

    @JvmStatic
    fun get(activity: Activity): List<FDialog>? {
        val holder = mapActivityDialog[activity] ?: return null
        return ArrayList(holder)
    }

    @JvmStatic
    fun getLast(activity: Activity): FDialog? {
        val holder = mapActivityDialog[activity] ?: return null
        return holder.lastOrNull()
    }

    @JvmStatic
    fun remove(activity: Activity) {
        mapActivityDialog.remove(activity)
    }
}