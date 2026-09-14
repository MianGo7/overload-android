package de.miangohar.overload.reminder

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import de.miangohar.overload.domain.logic.LoggingReminderAdvisor
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

/**
 * Schedules or cancels the daily logging reminder through WorkManager.
 *
 * The exact fire time is approximate, WorkManager may batch the work for
 * battery reasons, see ADR-0014. That is an accepted trade off for a
 * training reminder.
 */
object ReminderScheduler {

    private const val UNIQUE_WORK_NAME = "logging_reminder"

    /** Schedules the reminder for [reminderTime], or cancels it when null. */
    fun apply(context: Context, reminderTime: LocalTime?) {
        val workManager = WorkManager.getInstance(context)
        if (reminderTime == null) {
            workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
            return
        }

        val initialDelay = Duration.between(
            LocalDateTime.now(),
            LoggingReminderAdvisor.nextOccurrence(reminderTime, LocalDateTime.now()),
        )
        val request = PeriodicWorkRequestBuilder<LoggingReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay)
            .build()
        // UPDATE preserves the existing schedule's anchor rather than the
        // new initial delay, which is the wrong behaviour here: choosing a
        // new time is meant to replace the schedule outright.
        workManager.enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            request,
        )
    }
}
