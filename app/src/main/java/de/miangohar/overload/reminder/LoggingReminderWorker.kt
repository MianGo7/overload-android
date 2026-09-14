package de.miangohar.overload.reminder

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.miangohar.overload.OverloadApplication
import de.miangohar.overload.R
import de.miangohar.overload.domain.logic.LoggingReminderAdvisor
import de.miangohar.overload.ui.logentry.LogEntryActivity
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * Checks whether today still needs a nudge and posts the notification if so.
 *
 * Runs as a plain [CoroutineWorker] rather than through a dependency
 * injection framework, consistent with the rest of the app, see ADR-0004.
 */
class LoggingReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as OverloadApplication).container
        val goal = container.goalRepository.observeGoal().first()
        if (goal.reminderTime == null) return Result.success()

        val entries = container.setEntryRepository.observeAll().first()
        if (!LoggingReminderAdvisor.shouldRemind(entries, LocalDate.now())) return Result.success()

        postNotification()
        return Result.success()
    }

    private fun postNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        if (!notificationManager.areNotificationsEnabled()) return

        val openApp = Intent(applicationContext, LogEntryActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val contentIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            openApp,
            PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(applicationContext.getString(R.string.reminder_notification_title))
            .setContentText(applicationContext.getString(R.string.reminder_notification_body))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID = "logging_reminder"
        private const val NOTIFICATION_ID = 1
    }
}
