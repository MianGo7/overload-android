package de.miangohar.overload.domain.logic

import de.miangohar.overload.domain.model.SetEntry
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Decides whether and when the daily logging reminder should fire.
 *
 * A reminder scheduled for a day where something has already been logged
 * would be noise rather than help, so it is skipped.
 */
object LoggingReminderAdvisor {

    fun shouldRemind(entries: List<SetEntry>, today: LocalDate): Boolean =
        entries.none { it.date == today }

    /** The next point in time [time] occurs at or after [now], today or tomorrow. */
    fun nextOccurrence(time: LocalTime, now: LocalDateTime): LocalDateTime {
        val today = now.toLocalDate().atTime(time)
        return if (today.isAfter(now)) today else today.plusDays(1)
    }
}
