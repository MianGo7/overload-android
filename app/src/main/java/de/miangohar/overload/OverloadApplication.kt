package de.miangohar.overload

import android.app.Application
import de.miangohar.overload.di.AppContainer

/** Application entry point that owns the dependency container. */
class OverloadApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
