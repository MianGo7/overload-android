package de.miangohar.overload

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import de.miangohar.overload.di.AppContainer
import de.miangohar.overload.reminder.LoggingReminderWorker

/** Application entry point that owns the dependency container. */
class OverloadApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        createNotificationChannel()
    }

    // minSdk 26 is API level O, so notification channels are always available.
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            LoggingReminderWorker.CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = getString(R.string.notification_channel_description)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
