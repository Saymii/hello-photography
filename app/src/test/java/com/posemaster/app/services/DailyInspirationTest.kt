package com.posemaster.app.services

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.NotificationChannel
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.posemaster.app.data.AppPreferences
import org.json.JSONArray
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class DailyInspirationTest {
    private lateinit var context: Context

    @Before fun reset() {
        context = ApplicationProvider.getApplicationContext()
        AppPreferences(context).clear()
        DailyInspiration.cancel(context)
    }

    @Test fun schedulingAndCancellingLeaveNoRemindersBehind() {
        DailyInspiration.schedule(context)
        val alarms = shadowOf(context.getSystemService(AlarmManager::class.java))
        assertEquals(1, alarms.scheduledAlarms.size)
        DailyInspiration.cancel(context)
        assertTrue(alarms.scheduledAlarms.isEmpty())
    }

    @Test fun bootDoesNotOptUserIntoNotifications() {
        DailyInspirationReceiver().onReceive(context, Intent(Intent.ACTION_BOOT_COMPLETED))
        assertTrue(shadowOf(context.getSystemService(AlarmManager::class.java)).scheduledAlarms.isEmpty())
    }

    @Test fun disabledReminderChannelCannotEnableOrScheduleReminders() {
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel("daily_pose_inspiration", "Daily pose inspiration", NotificationManager.IMPORTANCE_NONE)
        )
        assertFalse(DailyInspiration.permitted(context))
        DailyInspiration.schedule(context)
        assertTrue(shadowOf(context.getSystemService(AlarmManager::class.java)).scheduledAlarms.isEmpty())
        DailyInspiration.deliver(context, "en")
        assertTrue(shadowOf(context.getSystemService(NotificationManager::class.java)).allNotifications.isEmpty())
    }

    @Test fun dailyNotificationOpensAnAvailableFreeGuide() {
        DailyInspiration.deliver(context, "en")
        val notification = shadowOf(context.getSystemService(NotificationManager::class.java)).allNotifications.single()
        val poseId = shadowOf(notification.contentIntent).savedIntent.getIntExtra("pose_id", -1)
        val json = JSONArray(context.assets.open("poses.json").bufferedReader().use { it.readText() })
        val selected = (0 until json.length()).map { json.getJSONObject(it) }.single { it.getInt("id") == poseId }
        assertFalse(selected.getBoolean("premium"))
        DailyInspiration.cancel(context)
        assertTrue(shadowOf(context.getSystemService(NotificationManager::class.java)).allNotifications.isEmpty())
    }
}
