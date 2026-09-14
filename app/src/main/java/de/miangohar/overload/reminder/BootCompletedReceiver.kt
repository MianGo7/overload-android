package de.miangohar.overload.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.miangohar.overload.OverloadApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Re-applies the saved reminder schedule after a reboot.
 *
 * WorkManager's own periodic work already survives a reboot in most cases,
 * this receiver exists so the schedule is rebuilt explicitly rather than
 * relying on that alone, and to use the broadcast receiver component the
 * course material covers.
 */
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val container = (appContext as OverloadApplication).container
                val goal = container.goalRepository.observeGoal().first()
                ReminderScheduler.apply(appContext, goal.reminderTime)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
