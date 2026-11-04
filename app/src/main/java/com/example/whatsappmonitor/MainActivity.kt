package com.example.whatsappmonitor

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var tokenInput: EditText
    private lateinit var chatIdInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("monitor_settings", MODE_PRIVATE)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 40, 32, 32)
        }

        val heading = TextView(this).apply {
            text = "WhatsApp Monitor"
            textSize = 24f
        }
        val explanation = TextView(this).apply {
            text = "يحفظ نص إشعارات واتساب التي تظهر على هذا الهاتف، ويمكنه إرسالها إلى بوت تليجرام. لا يسترجع الرسائل التي لم يصل إشعارها، ولا يفتح صور العرض لمرة واحدة."
            textSize = 16f
            setPadding(0, 16, 0, 20)
        }

        tokenInput = EditText(this).apply {
            hint = "Telegram Bot Token"
            singleLine = true
            setText(prefs.getString("bot_token", ""))
        }
        chatIdInput = EditText(this).apply {
            hint = "Telegram Chat ID"
            singleLine = true
            setText(prefs.getString("chat_id", ""))
        }

        val save = Button(this).apply {
            text = "حفظ إعدادات تليجرام"
            setOnClickListener {
                val token = tokenInput.text.toString().trim()
                val chatId = chatIdInput.text.toString().trim()
                if (token.isBlank() || chatId.isBlank()) {
                    Toast.makeText(this@MainActivity, "أدخل التوكن و Chat ID أولاً", Toast.LENGTH_LONG).show()
                } else {
                    prefs.edit().putString("bot_token", token).putString("chat_id", chatId).apply()
                    Toast.makeText(this@MainActivity, "تم حفظ الإعدادات على هذا الهاتف", Toast.LENGTH_LONG).show()
                }
            }
        }

        val access = Button(this).apply {
            text = "تفعيل صلاحية الوصول للإشعارات"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        }

        val howTo = TextView(this).apply {
            text = "طريقة الإعداد:\n1) أنشئ بوتاً عبر @BotFather وخذ Bot Token.\n2) افتح محادثة البوت واضغط Start.\n3) استخرج Chat ID باستخدام طريقة getUpdates في شرح المشروع.\n4) أدخل القيم أعلاه واحفظها.\n5) فعّل صلاحية الإشعارات لهذا التطبيق من إعدادات أندرويد."
            textSize = 14f
            setPadding(0, 20, 0, 0)
        }

        listOf(heading, explanation, tokenInput, chatIdInput, save, access, howTo).forEach {
            layout.addView(it, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        setContentView(layout)
    }
}