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
            holder = HashSet()
            mapActivityDialog[activity] = holder
        }
        holder.add(dialog)
    }

    @JvmStatic
    fun removeDialog(dialog: FDialog) {
        val activity = dialog.ownerActivity
        val holder = mapActivityDialog[activity] ?: return

        holder.remove(dialog)
        if (holder.isEmpty()) {
            mapActivityDialog.remove(activity)
        }
    }

    @JvmStatic
    operator fun get(activity: Activity): List<FDialog>? {
        val holder = mapActivityDialog[activity] ?: return null
        return ArrayList(holder)
    }

    @JvmStatic
    fun remove(activity: Activity) {
        mapActivityDialog.remove(activity)
    }
}