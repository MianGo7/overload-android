package de.miangohar.overload.data

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.miangohar.overload.data.local.AppDatabase
import de.miangohar.overload.data.local.MIGRATION_1_2
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The first real schema change this project has made, see ADR-0014. The
 * migration is tested against the previous exported schema rather than
 * written blind, and destructive fallback is never used.
 */
@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
    )

    @Test
    fun migrate1To2PreservesTheExistingGoalAndAddsANullableReminderColumn() {
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL("INSERT INTO training_goal (id, phase, block_length_weeks) VALUES (1, 'BULK', 5)")
            close()
        }

        val migrated = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        migrated.query("SELECT phase, block_length_weeks, reminder_time FROM training_goal WHERE id = 1").use { row ->
            assertTrue(row.moveToFirst())
            assertEquals("BULK", row.getString(row.getColumnIndexOrThrow("phase")))
            assertEquals(5, row.getInt(row.getColumnIndexOrThrow("block_length_weeks")))
            assertTrue(row.isNull(row.getColumnIndexOrThrow("reminder_time")))
        }
    }

    private companion object {
        const val TEST_DB = "migration-test"
    }
}
