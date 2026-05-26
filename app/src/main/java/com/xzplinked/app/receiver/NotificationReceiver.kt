package com.xzplinked.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.xzplinked.app.service.AudioPlaybackService

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        val serviceIntent = Intent(context, AudioPlaybackService::class.java).apply {
            this.action = action
        }

        when (action) {
            AudioPlaybackService.ACTION_PLAY -> {
                context.startService(serviceIntent)
            }
            AudioPlaybackService.ACTION_PAUSE -> {
                context.startService(serviceIntent)
            }
            AudioPlaybackService.ACTION_RESUME -> {
                context.startService(serviceIntent)
            }
            AudioPlaybackService.ACTION_STOP -> {
                context.startService(serviceIntent)
            }
            AudioPlaybackService.ACTION_NEXT -> {
                context.startService(serviceIntent)
            }
            AudioPlaybackService.ACTION_PREV -> {
                context.startService(serviceIntent)
            }
        }
    }
}
