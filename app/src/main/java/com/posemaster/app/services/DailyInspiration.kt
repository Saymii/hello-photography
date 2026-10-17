package com.posemaster.app.services

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.posemaster.app.MainActivity
import com.posemaster.app.R
import com.posemaster.app.data.AppPreferences
import org.json.JSONArray
import java.time.LocalDate
import java.time.ZonedDateTime

/** Local opt-in reminders. Inexact alarms need no exact-alarm permission or cloud service. */
object DailyInspiration {
    private const val ACTION = "com.posemaster.app.DAILY_INSPIRATION"
    private const val REQUEST_CODE = 410
    private const val CHANNEL = "daily_pose_inspiration"
    private const val NOTIFICATION_ID = 411

    fun permitted(context: Context): Boolean {
        val runtimeGranted = Build.VERSION.SDK_INT < 33 || context.checkSelfPermission(
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        val manager = context.getSystemService(NotificationManager::class.java)
        if (!runtimeGranted || !manager.areNotificationsEnabled()) return false
        val channel = manager.getNotificationChannel(CHANNEL) ?: return true
        if (channel.importance == NotificationManager.IMPORTANCE_NONE) return false
        if (Build.VERSION.SDK_INT >= 28 && channel.group != null &&
            manager.getNotificationChannelGroup(channel.group)?.isBlocked == true) return false
        return true
    }

    private fun alarmIntent(context: Context) = PendingIntent.getBroadcast(
        context, REQUEST_CODE,
        Intent(context, DailyInspirationReceiver::class.java).setAction(ACTION),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    /** A one-hour delivery window beginning at 9am local time; Android may delay it in idle mode. */
    fun schedule(context: Context) {
        if (!permitted(context)) return
        val now = ZonedDateTime.now()
        var next = now.withHour(9).withMinute(0).withSecond(0).withNano(0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        context.getSystemService(AlarmManager::class.java).setWindow(
            AlarmManager.RTC_WAKEUP, next.toInstant().toEpochMilli(), 60 * 60 * 1000L,
            alarmIntent(context)
        )
    }

    fun cancel(context: Context) {
        context.getSystemService(AlarmManager::class.java).cancel(alarmIntent(context))
        context.getSystemService(NotificationManager::class.java).cancel(NOTIFICATION_ID)
    }

    internal fun deliver(context: Context, language: String) {
        if (!permitted(context)) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val amharic = language == "am"
        manager.createNotificationChannel(NotificationChannel(
            CHANNEL, if (amharic) "የዕለቱ አነሳሽ ሐሳብ" else "Daily pose inspiration",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "One optional daily photography tip, around 9am local time." })
        // Select only free guides. Bundled content is available without internet or an account.
        val guides = runCatching {
            val array = JSONArray(context.assets.open("poses.json").bufferedReader().use { it.readText() })
            (0 until array.length()).map { array.getJSONObject(it) }.filter { !it.optBoolean("premium") }
        }.getOrElse { emptyList() }
        if (guides.isEmpty()) return
        val guide = guides[(LocalDate.now().dayOfYear - 1) % guides.size]
        val translated = if (amharic) runCatching {
            val array = JSONArray(context.assets.open("poses-am.json").bufferedReader().use { it.readText() })
            (0 until array.length()).map { array.getJSONObject(it) }
                .firstOrNull { it.optInt("id") == guide.getInt("id") }
        }.getOrNull() else null
        val open = PendingIntent.getActivity(context, guide.getInt("id"),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("pose_id", guide.getInt("id"))
            }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val copy = translated?.optString("steps")?.takeIf { it.isNotBlank() } ?: guide.getString("steps")
        val name = translated?.optString("name")?.takeIf { it.isNotBlank() } ?: guide.getString("name")
        val notification = Notification.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_notification_camera)
            .setContentTitle(if (amharic) "የዕለቱ ፖዝ፦ $name" else "Today’s pose: $name")
            .setContentText(copy)
            .setStyle(Notification.BigTextStyle().bigText(copy))
            .setContentIntent(open).setAutoCancel(true).build()
        try { manager.notify(NOTIFICATION_ID, notification) } catch (_: SecurityException) {
            // A permission can be revoked between the check and notification delivery.
        }
    }
}

class DailyInspirationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val preferences = AppPreferences(context)
        if (!preferences.state.value.notificationsEnabled) return
        if (intent.action == "com.posemaster.app.DAILY_INSPIRATION") {
            DailyInspiration.deliver(context, preferences.state.value.language)
        }
        DailyInspiration.schedule(context)
    }
}
