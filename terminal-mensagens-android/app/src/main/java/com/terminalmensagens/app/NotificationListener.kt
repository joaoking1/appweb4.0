package com.terminalmensagens.app

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class NotificationListener : NotificationListenerService() {

    private val allowedPackages = mapOf(
        "com.whatsapp" to "WhatsApp",
        "com.whatsapp.w4b" to "WhatsApp Business",
        "com.instagram.android" to "Instagram"
    )

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val notif = sbn ?: return
        val app = allowedPackages[notif.packageName] ?: return

        val extras = notif.notification?.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()

        // Ignore silent group summaries / empty
        if (title.isNullOrBlank() && text.isNullOrBlank()) return

        val iso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(notif.postTime))

        val ctx = applicationContext
        val token = DeviceManager.deviceToken(ctx)
        val name = DeviceManager.deviceName()
        val baseUrl = DeviceManager.apiUrl(ctx)

        CoroutineScope(Dispatchers.IO).launch {
            ApiClient.sendMessage(baseUrl, token, name, app, title, text, iso)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) { /* no-op */ }
}
