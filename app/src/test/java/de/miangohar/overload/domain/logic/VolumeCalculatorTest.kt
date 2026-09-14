package de.miangohar.overload.domain.logic

import de.miangohar.overload.domain.TestFixtures
import de.miangohar.overload.domain.model.MuscleGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VolumeCalculatorTest {

    private val delta = 0.0001

    @Test
    fun `a set counts once for the primary muscle group`() {
        val entries = listOf(TestFixtures.entry(primary = MuscleGroup.CHEST, sets = 4))

        val volume = VolumeCalculator.weeklyVolume(entries, TestFixtures.WEEK)

        assertEquals(4.0, volume.getValue(MuscleGroup.CHEST), delta)
    }

    @Test
    fun `a set counts half for every secondary muscle group`() {
        val entries = listOf(
            TestFixtures.entry(
                primary = MuscleGroup.CHEST,
                secondary = setOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS_SIDE),
                sets = 4,
            ),
        )

        val volume = VolumeCalculator.weeklyVolume(entries, TestFixtures.WEEK)

        assertEquals(4.0, volume.getValue(MuscleGroup.CHEST), delta)
        assertEquals(2.0, volume.getValue(MuscleGroup.TRICEPS), delta)
        assertEquals(2.0, volume.getValue(MuscleGroup.SHOULDERS_SIDE), delta)
    }

    @Test
    fun `volume of several entries for the same muscle group accumulates`() {
        val entries = listOf(
            TestFixtures.entry(primary = MuscleGroup.BACK, sets = 3),
            TestFixtures.entry(
                date = TestFixtures.MONDAY.plusDays(3),
                primary = MuscleGroup.BICEPS,
                secondary = setOf(MuscleGroup.BACK),
                sets = 3,
            ),
        )

        val volume = VolumeCalculator.weeklyVolume(entries, TestFixtures.WEEK)

        assertEquals(4.5, volume.getValue(MuscleGroup.BACK), delta)
        assertEquals(3.0, volume.getValue(MuscleGroup.BICEPS), delta)
    }

    @Test
    fun `entries outside the week are ignored`() {
        val entries = listOf(
            TestFixtures.entry(date = TestFixtures.MONDAY.minusDays(1), sets = 5),
            TestFixtures.entry(date = TestFixtures.MONDAY.plusDays(7), sets = 5),
        )

        val volume = VolumeCalculator.weeklyVolume(entries, TestFixtures.WEEK)

        assertTrue(volume.isEmpty())
    }

    @Test
    fun `total sets sums the fractional volume of all muscle groups`() {
        val entries = listOf(
            TestFixtures.entry(
                primary = MuscleGroup.CHEST,
                secondary = setOf(MuscleGroup.TRICEPS),
                sets = 3,
            ),
        )

        assertEquals(4.5, VolumeCalculator.totalSets(entries, TestFixtures.WEEK), delta)
    }

    @Test
    fun `volume load multiplies sets reps and load`() {
        val entries = listOf(TestFixtures.entry(sets = 3, reps = 10, weightKg = 60.0))

        assertEquals(1800.0, VolumeCalculator.volumeLoadKg(entries, TestFixtures.WEEK), delta)
    }

    @Test
    fun `weekly totals are grouped by week and ordered from oldest to newest`() {
        val entries = listOf(
            TestFixtures.entry(date = TestFixtures.MONDAY.plusDays(7), sets = 2),
            TestFixtures.entry(date = TestFixtures.MONDAY, sets = 3),
        )

        val totals = VolumeCalculator.weeklyTotals(entries)

        assertEquals(2, totals.size)
        assertEquals(TestFixtures.MONDAY, totals.first().week.startDate)
        assertEquals(3.0, totals.first().totalSets, delta)
        assertEquals(2.0, totals.last().totalSets, delta)
    }

    @Test
    fun `recent weekly totals cover exactly weekCount weeks ending at the current week`() {
        val currentWeek = TestFixtures.WEEK

        val totals = VolumeCalculator.recentWeeklyTotals(emptyList(), currentWeek, weekCount = 12)

        assertEquals(12, totals.size)
        assertEquals(currentWeek, totals.last().week)
        assertEquals(currentWeek.startDate.minusWeeks(11), totals.first().week.startDate)
    }

    @Test
    fun `recent weekly totals fill weeks without any entry with zero`() {
        val entries = listOf(TestFixtures.entry(date = TestFixtures.MONDAY, sets = 4))

        val totals = VolumeCalculator.recentWeeklyTotals(entries, TestFixtures.WEEK, weekCount = 3)

        assertEquals(listOf(0.0, 0.0, 4.0), totals.map { it.totalSets })
    }

    @Test
    fun `recent weekly totals filter to a single muscle group when given one`() {
        val entries = listOf(
            TestFixtures.entry(
                date = TestFixtures.MONDAY,
                primary = MuscleGroup.CHEST,
                secondary = setOf(MuscleGroup.TRICEPS),
                sets = 4,
            ),
        )

        val chest = VolumeCalculator.recentWeeklyTotals(
            entries,
            TestFixtures.WEEK,
            weekCount = 1,
            muscleGroup = MuscleGroup.CHEST,
        )
        val triceps = VolumeCalculator.recentWeeklyTotals(
            entries,
            TestFixtures.WEEK,
            weekCount = 1,
            muscleGroup = MuscleGroup.TRICEPS,
        )

        assertEquals(4.0, chest.single().totalSets, delta)
        assertEquals(2.0, triceps.single().totalSets, delta)
    }
}
