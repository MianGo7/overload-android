package de.miangohar.overload.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.miangohar.overload.data.local.dao.GoalDao
import de.miangohar.overload.data.local.dao.SetEntryDao
import de.miangohar.overload.data.local.entity.SetEntryEntity
import de.miangohar.overload.data.local.entity.TrainingGoalEntity
import de.miangohar.overload.data.local.entity.VolumeTargetEntity

/**
 * The single SQLite database of the app.
 *
 * Schemas are exported to app/schemas so that a future migration can be tested
 * against the previous version instead of being written blind.
 */
@Database(
    entities = [
        SetEntryEntity::class,
        TrainingGoalEntity::class,
        VolumeTargetEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun setEntryDao(): SetEntryDao

    abstract fun goalDao(): GoalDao

    companion object {
        const val NAME = "overload.db"
    }
}
