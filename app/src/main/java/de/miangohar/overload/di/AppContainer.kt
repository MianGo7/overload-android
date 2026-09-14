package de.miangohar.overload.di

import android.content.Context
import androidx.room.Room
import de.miangohar.overload.data.local.AppDatabase
import de.miangohar.overload.data.local.MIGRATION_1_2
import de.miangohar.overload.data.repository.RoomGoalRepository
import de.miangohar.overload.data.repository.RoomSetEntryRepository
import de.miangohar.overload.domain.repository.GoalRepository
import de.miangohar.overload.domain.repository.SetEntryRepository

/**
 * Manual dependency container.
 *
 * The project deliberately wires its few dependencies by hand instead of
 * pulling in a dependency injection framework. The graph is small, every
 * instance created here is visible in one file, and there is no annotation
 * processing to explain. See docs/decision-log.md, entry ADR-0004.
 */
class AppContainer(context: Context) {

    private val applicationContext: Context = context.applicationContext

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, AppDatabase.NAME)
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    val setEntryRepository: SetEntryRepository by lazy {
        RoomSetEntryRepository(database.setEntryDao())
    }

    val goalRepository: GoalRepository by lazy {
        RoomGoalRepository(database, database.goalDao())
    }
}
