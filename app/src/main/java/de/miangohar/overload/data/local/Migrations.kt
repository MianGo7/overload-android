package de.miangohar.overload.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Adds the nullable reminder time to the goal header, see ADR-0014. */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE training_goal ADD COLUMN reminder_time INTEGER")
    }
}
