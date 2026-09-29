package com.example.lifedots.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.lifedots.preferences.LifeDotsPreferences

class DateChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIME_SET -> {
                // Trigger wallpaper redraw by notifying preferences change listeners
                // This will cause the wallpaper service to refresh
                val prefs = LifeDotsPreferences.getInstance(context)
                prefs.notifyDateChanged()
            }
        }
    }
}
