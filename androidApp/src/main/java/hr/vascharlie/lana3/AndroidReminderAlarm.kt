package hr.vascharlie.lana3

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
import hr.vascharlie.lana3.core.reminders.LanaReminder

/**
 * Local, inexact alarms. The device may delay them (Doze, battery restrictions).
 * No promise of delivery at a precise minute; intended for day-level advance notice.
 */
object AndroidReminderAlarm {
    private const val EXTRA_REMINDER_ID = "reminder_id"
    private const val CHANNEL = "lana_local_reminders"

    fun schedule(context: Context, reminder: LanaReminder) {
        cancel(context, reminder.id)
        val at = reminder.notifyAtEpochMillis ?: return
        if (reminder.completed || at <= System.currentTimeMillis()) return
        val alarm = context.getSystemService(AlarmManager::class.java) ?: return
        val pending = PendingIntent.getBroadcast(
            context, requestCode(reminder.id), intent(context, reminder.id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending)
    }

    fun cancel(context: Context, id: String) {
        val existing = PendingIntent.getBroadcast(
            context, requestCode(id), intent(context, id),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        context.getSystemService(AlarmManager::class.java)?.cancel(existing)
        existing.cancel()
    }

    private fun requestCode(id: String): Int = id.hashCode() and Int.MAX_VALUE

    private fun intent(context: Context, id: String): Intent =
        Intent(context, ReminderAlarmReceiver::class.java).putExtra(EXTRA_REMINDER_ID, id)

    internal fun notify(context: Context, id: String) {
        AndroidReminderStore(context).use { store ->
            val reminder = store.findById(id) ?: return
            if (reminder.completed || reminder.notifyAtEpochMillis == null) return
            if (Build.VERSION.SDK_INT >= 33 &&
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) return

            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL,
                    context.getString(R.string.reminders_notification_channel),
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
            val open = PendingIntent.getActivity(
                context, requestCode(id),
                Intent(context, RemindersActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val notification = Notification.Builder(context, CHANNEL)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(context.getString(R.string.reminders_notification_title))
                .setContentText(reminder.title)
                .setContentIntent(open)
                .setVisibility(Notification.VISIBILITY_PRIVATE)
                .setAutoCancel(true)
                .build()
            manager.notify(requestCode(id), notification)
        }
    }
}

class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        intent.getStringExtra("reminder_id")?.let { AndroidReminderAlarm.notify(context, it) }
    }
}

class ReminderRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return
        AndroidReminderStore(context).use { store ->
            store.listAll().filterNot { it.completed }.forEach { reminder ->
                AndroidReminderAlarm.schedule(context, reminder)
            }
        }
    }
}
