package de.miangohar.overload.domain.logic

import de.miangohar.overload.domain.TestFixtures
import de.miangohar.overload.domain.model.TrainingWeek
import de.miangohar.overload.domain.model.WeeklyTotal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeloadAdvisorTest {

    private val goal = TestFixtures.goal(blockLengthWeeks = 5)

    private fun totals(vararg sets: Double): List<WeeklyTotal> =
        sets.mapIndexed { index, value ->
            WeeklyTotal(TrainingWeek(TestFixtures.MONDAY.plusWeeks(index.toLong())), value)
        }

    @Test
    fun `an empty history yields no recommendation`() {
        val recommendation = DeloadAdvisor.recommend(emptyList(), goal)

        assertFalse(recommendation.isDue)
        assertEquals(DeloadReason.NO_HISTORY, recommendation.reason)
    }

    @Test
    fun `a deload is not due inside the accumulation block`() {
        val recommendation = DeloadAdvisor.recommend(totals(100.0, 105.0, 110.0), goal)

        assertFalse(recommendation.isDue)
        assertEquals(3, recommendation.accumulationWeeks)
        assertEquals(DeloadReason.WITHIN_BLOCK, recommendation.reason)
    }

    @Test
    fun `a deload is due once the block length is reached`() {
        val recommendation = DeloadAdvisor.recommend(
            totals(100.0, 105.0, 110.0, 108.0, 112.0),
            goal,
        )

        assertTrue(recommendation.isDue)
        assertEquals(5, recommendation.accumulationWeeks)
        assertEquals(DeloadReason.BLOCK_COMPLETE, recommendation.reason)
    }

    @Test
    fun `a week at half the trailing volume is recognised as a deload`() {
        val recommendation = DeloadAdvisor.recommend(
            totals(100.0, 100.0, 100.0, 40.0),
            goal,
        )

        assertFalse(recommendation.isDue)
        assertEquals(0, recommendation.accumulationWeeks)
        assertEquals(DeloadReason.DELOAD_IN_PROGRESS, recommendation.reason)
    }

    @Test
    fun `the accumulation count restarts after a deload week`() {
        val recommendation = DeloadAdvisor.recommend(
            totals(100.0, 100.0, 100.0, 40.0, 100.0, 105.0),
            goal,
        )

        assertFalse(recommendation.isDue)
        assertEquals(2, recommendation.accumulationWeeks)
    }

    @Test
    fun `the very first logged week is never treated as a deload`() {
        val recommendation = DeloadAdvisor.recommend(totals(20.0), goal)

        assertEquals(1, recommendation.accumulationWeeks)
        assertEquals(DeloadReason.WITHIN_BLOCK, recommendation.reason)
    }

    @Test
    fun `totals given out of order are sorted before they are judged`() {
        val ordered = totals(100.0, 100.0, 100.0, 40.0)

        val recommendation = DeloadAdvisor.recommend(ordered.reversed(), goal)

        assertEquals(DeloadReason.DELOAD_IN_PROGRESS, recommendation.reason)
    }
}
