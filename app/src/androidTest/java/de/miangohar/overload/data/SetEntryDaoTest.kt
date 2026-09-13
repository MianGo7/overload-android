package de.miangohar.overload.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.miangohar.overload.data.local.AppDatabase
import de.miangohar.overload.data.local.dao.SetEntryDao
import de.miangohar.overload.data.local.entity.SetEntryEntity
import de.miangohar.overload.domain.model.MuscleGroup
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * Verifies the parts that plain JVM tests cannot reach: the generated Room
 * queries and the type converters behind them. The database runs in memory,
 * so the test leaves nothing behind on the device.
 */
@RunWith(AndroidJUnit4::class)
class SetEntryDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: SetEntryDao

    private val monday: LocalDate = LocalDate.of(2026, 1, 5)

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.setEntryDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertedEntryIsReadBackWithAllConvertedFields() = runTest {
        val id = dao.insert(entity(date = monday))

        val stored = dao.observeAll().first().single()

        assertEquals(id, stored.id)
        assertEquals(monday, stored.date)
        assertEquals(MuscleGroup.CHEST, stored.primaryMuscle)
        assertEquals(setOf(MuscleGroup.TRICEPS), stored.secondaryMuscles)
    }

    @Test
    fun weekQueryReturnsOnlyEntriesInsideTheRange() = runTest {
        dao.insert(entity(date = monday.minusDays(1)))
        dao.insert(entity(date = monday))
        dao.insert(entity(date = monday.plusDays(6)))
        dao.insert(entity(date = monday.plusDays(7)))

        val stored = dao.observeBetween(monday, monday.plusDays(6)).first()

        assertEquals(2, stored.size)
        assertTrue(stored.all { it.date in monday..monday.plusDays(6) })
    }

    @Test
    fun deletedEntryDisappearsFromTheStream() = runTest {
        val id = dao.insert(entity(date = monday))

        dao.deleteById(id)

        assertTrue(dao.observeAll().first().isEmpty())
    }

    private fun entity(date: LocalDate) = SetEntryEntity(
        date = date,
        exerciseName = "Bench Press",
        primaryMuscle = MuscleGroup.CHEST,
        secondaryMuscles = setOf(MuscleGroup.TRICEPS),
        sets = 3,
        reps = 10,
        weightKg = 60.0,
        rir = 2,
    )
}
