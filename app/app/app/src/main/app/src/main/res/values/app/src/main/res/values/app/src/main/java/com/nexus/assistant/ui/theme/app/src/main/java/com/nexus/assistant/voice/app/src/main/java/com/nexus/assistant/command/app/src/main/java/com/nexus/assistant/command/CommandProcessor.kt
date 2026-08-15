package com.nexus.assistant.command

import android.content.Context
import android.content.Intent

data class CommandResult(
    val spokenReply: String,
    val success: Boolean
)

object CommandProcessor {

    private val openPatterns = listOf(
        Regex("^افتح (?:لي )?(.+)$"),
        Regex("^شغل (.+)$"),
        Regex("^open (.+)$"),
        Regex("^launch (.+)$")
    )

    private val notImplementedKeywords = mapOf(
        Regex("screenshot|سكرين ?شوت|لقطة شاشة") to "أخذ لقطة الشاشة محتاج صلاحية خاصة (MediaProjection) — ميزة قادمة في مرحلة لاحقة، مو مفعّلة بعد.",
        Regex("gmail|ايميل|إيميل|البريد") to "الاتصال بجيميل يحتاج ربط حساب Google (OAuth) وسيرفر خلفي — مو مفعّل بعد في هذي المرحلة.",
        Regex("اتصل|call ") to "إجراء المكالمات يحتاج صلاحية إضافية ومراجعة أمنية — مو مفعّل بعد في هذي المرحلة.",
        Regex("رسالة|sms|message") to "إرسال الرسائل يحتاج صلاحية إضافية وتأكيد أمان — مو مفعّل بعد في هذي المرحلة."
    )

    fun process(context: Context, rawText: String): CommandResult {
        val text = rawText.trim()
        if (text.isEmpty()) {
            return CommandResult("ما سمعت أمر واضح", false)
        }

        for ((pattern, message) in notImplementedKeywords) {
            if (pattern.containsMatchIn(text.lowercase())) {
                return CommandResult(message, false)
            }
        }

        for (pattern in openPatterns) {
            val match = pattern.find(text.lowercase()) ?: pattern.find(text)
            if (match != null) {
                val appName = match.groupValues[1].trim()
                return openApp(context, appName)
            }
        }

        return CommandResult(
            "هذا الأمر مو مدعوم بعد. حاليًا أقدر بس أفتح تطبيقات، مثلاً \"افتح واتساب\".",
            false
        )
    }

    private fun openApp(context: Context, appName: String): CommandResult {
        return when (val resolution = AppCatalog.resolve(context, appName)) {
            is AppCatalog.Resolution.Found -> {
                try {
                    resolution.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(resolution.intent)
                    CommandResult("تم فتح ${resolution.label}", true)
                } catch (e: Exception) {
                    CommandResult("لقيت ${resolution.label} بس ما قدرت أفتحه", false)
                }
            }
            AppCatalog.Resolution.NotFound -> {
                CommandResult("ما لقيت تطبيق اسمه \"$appName\" على جهازك", false)
            }
        }
    }
}
