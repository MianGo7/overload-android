package de.miangohar.overload.domain.logic

import de.miangohar.overload.domain.model.MuscleGroup
import de.miangohar.overload.domain.model.TrainingWeek
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SeedDataGeneratorTest {

    private val today = LocalDate.of(2026, 9, 17)

    @Test
    fun `covers between six and eight distinct weeks`() {
        val data = SeedDataGenerator.generate(today)

        val weeks = data.entries.map { TrainingWeek.containing(it.date) }.distinct()

        assertTrue(weeks.size in 6..8)
    }

    @Test
    fun `the most recent week has entries`() {
        val data = SeedDataGenerator.generate(today)
        val currentWeek = TrainingWeek.containing(today)

        assertTrue(data.entries.any { it.date in currentWeek })
    }

    @Test
    fun `every muscle group appears somewhere in the history`() {
        val data = SeedDataGenerator.generate(today)

        val covered = data.entries.flatMap { listOf(it.primaryMuscle) + it.secondaryMuscles }.toSet()

        assertEquals(MuscleGroup.entries.toSet(), covered)
    }

    @Test
    fun `one week is a genuine deload relative to its trailing average`() {
        val data = SeedDataGenerator.generate(today)

        val totals = VolumeCalculator.weeklyTotals(data.entries)
        val hasDeloadWeek = totals.indices.any { DeloadAdvisor.isDeloadWeek(totals, it) }

        assertTrue(hasDeloadWeek)
    }

    @Test
    fun `the advisor has a demonstrable recommendation for the most recent week`() {
        val data = SeedDataGenerator.generate(today)

        val totals = VolumeCalculator.weeklyTotals(data.entries)
        val recommendation = DeloadAdvisor.recommend(totals, data.goal)

        assertNotEquals(DeloadReason.NO_HISTORY, recommendation.reason)
        assertNotEquals(DeloadReason.WITHIN_BLOCK, recommendation.reason)
    }
}
