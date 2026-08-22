package com.nexus.assistant.command

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

object AppCatalog {

    private val knownApps = mapOf(
        "واتساب" to "com.whatsapp", "whatsapp" to "com.whatsapp",
        "تيليجرام" to "org.telegram.messenger", "telegram" to "org.telegram.messenger",
        "يوتيوب" to "com.google.android.youtube", "youtube" to "com.google.android.youtube",
        "جيميل" to "com.google.android.gm", "gmail" to "com.google.android.gm",
        "كروم" to "com.android.chrome", "chrome" to "com.android.chrome",
        "الكاميرا" to "camera", "كاميرا" to "camera", "camera" to "camera",
        "الإعدادات" to "settings", "اعدادات" to "settings", "settings" to "settings",
        "فيسبوك" to "com.facebook.katana", "facebook" to "com.facebook.katana",
        "انستغرام" to "com.instagram.android", "instagram" to "com.instagram.android",
        "خرائط جوجل" to "com.google.android.apps.maps", "maps" to "com.google.android.apps.maps",
        "التقويم" to "com.google.android.calendar", "calendar" to "com.google.android.calendar",
        "الحاسبة" to "com.google.android.calculator", "calculator" to "com.google.android.calculator"
    )

    sealed class Resolution {
        data class Found(val intent: Intent, val label: String) : Resolution()
        object NotFound : Resolution()
    }

    fun resolve(context: Context, rawName: String): Resolution {
        val pm = context.packageManager
        val key = rawName.trim().lowercase()

        knownApps[key]?.let { target ->
            when (target) {
                "camera" -> return Resolution.Found(
                    Intent(android.provider.MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA),
                    "الكاميرا"
                )
                "settings" -> return Resolution.Found(
                    Intent(android.provider.Settings.ACTION_SETTINGS),
                    "الإعدادات"
                )
                else -> {
                    pm.getLaunchIntentForPackage(target)?.let { intent ->
                        return Resolution.Found(intent, labelFor(pm, target) ?: rawName)
                    }
                }
            }
        }

        val launchIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val candidates = pm.queryIntentActivities(launchIntent, PackageManager.MATCH_DEFAULT_ONLY)
        for (info in candidates) {
            val label = info.loadLabel(pm).toString()
            if (label.lowercase().contains(key) || key.contains(label.lowercase())) {
                val intent = pm.getLaunchIntentForPackage(info.activityInfo.packageName)
                if (intent != null) return Resolution.Found(intent, label)
            }
        }

        return Resolution.NotFound
    }

    private fun labelFor(pm: PackageManager, packageName: String): String? = try {
        pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        null
    }
}
