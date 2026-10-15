package com.liteplanner.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("EXTRA_TITLE") ?: "日程消息提醒"
        val message = intent.getStringExtra("EXTRA_MESSAGE") ?: "您有一项待办或日程即将开始"
        val notificationId = intent.getIntExtra("EXTRA_ID", System.currentTimeMillis().toInt())

        NotificationHelper.showInstantNotification(
            context = context,
            notificationId = notificationId,
            title = title,
            message = message
        )
    }
}
