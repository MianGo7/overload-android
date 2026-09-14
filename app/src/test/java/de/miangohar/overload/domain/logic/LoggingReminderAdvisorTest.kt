package de.miangohar.overload.domain.logic

import de.miangohar.overload.domain.TestFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.LocalTime

class LoggingReminderAdvisorTest {

    @Test
    fun `reminds when nothing was logged today`() {
        val entries = listOf(TestFixtures.entry(date = TestFixtures.MONDAY.minusDays(1)))

        assertTrue(LoggingReminderAdvisor.shouldRemind(entries, TestFixtures.MONDAY))
    }

    @Test
    fun `does not remind when something was logged today`() {
        val entries = listOf(TestFixtures.entry(date = TestFixtures.MONDAY))

        assertFalse(LoggingReminderAdvisor.shouldRemind(entries, TestFixtures.MONDAY))
    }

    @Test
    fun `reminds when there is no history at all`() {
        assertTrue(LoggingReminderAdvisor.shouldRemind(emptyList(), TestFixtures.MONDAY))
    }

    @Test
    fun `next occurrence is later today when the time has not passed yet`() {
        val now = LocalDateTime.of(TestFixtures.MONDAY, LocalTime.of(8, 0))

        val next = LoggingReminderAdvisor.nextOccurrence(LocalTime.of(18, 0), now)

        assertEquals(LocalDateTime.of(TestFixtures.MONDAY, LocalTime.of(18, 0)), next)
    }

    @Test
    fun `next occurrence is tomorrow when the time has already passed`() {
        val now = LocalDateTime.of(TestFixtures.MONDAY, LocalTime.of(20, 0))

        val next = LoggingReminderAdvisor.nextOccurrence(LocalTime.of(18, 0), now)

        assertEquals(LocalDateTime.of(TestFixtures.MONDAY.plusDays(1), LocalTime.of(18, 0)), next)
    }
}
