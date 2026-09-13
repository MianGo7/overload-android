package de.miangohar.overload.domain.model

import de.miangohar.overload.domain.TestFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TrainingWeekTest {

    @Test
    fun `week always starts on the monday of the given date`() {
        val wednesday = TestFixtures.MONDAY.plusDays(2)

        assertEquals(TestFixtures.MONDAY, TrainingWeek.containing(wednesday).startDate)
    }

    @Test
    fun `sunday still belongs to the week that started on monday`() {
        val sunday = TestFixtures.MONDAY.plusDays(6)

        assertEquals(TestFixtures.MONDAY, TrainingWeek.containing(sunday).startDate)
        assertTrue(sunday in TestFixtures.WEEK)
    }

    @Test
    fun `the following monday is outside the week`() {
        assertFalse(TestFixtures.MONDAY.plusDays(7) in TestFixtures.WEEK)
    }

    @Test
    fun `previous and next shift the week by seven days`() {
        assertEquals(TestFixtures.MONDAY.minusDays(7), TestFixtures.WEEK.previous().startDate)
        assertEquals(TestFixtures.MONDAY.plusDays(7), TestFixtures.WEEK.next().startDate)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a week cannot start on a day other than monday`() {
        TrainingWeek(LocalDate.of(2026, 1, 6))
    }
}
