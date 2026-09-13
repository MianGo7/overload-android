package de.miangohar.overload.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * A training week, always starting on a Monday so that weekly volume is
 * comparable across the whole history.
 */
data class TrainingWeek(val startDate: LocalDate) : Comparable<TrainingWeek> {

    init {
        require(startDate.dayOfWeek == DayOfWeek.MONDAY) { "startDate must be a Monday" }
    }

    val endDate: LocalDate get() = startDate.plusDays(DAYS_PER_WEEK - 1L)

    operator fun contains(date: LocalDate): Boolean =
        !date.isBefore(startDate) && !date.isAfter(endDate)

    fun previous(): TrainingWeek = TrainingWeek(startDate.minusWeeks(1))

    fun next(): TrainingWeek = TrainingWeek(startDate.plusWeeks(1))

    override fun compareTo(other: TrainingWeek): Int = startDate.compareTo(other.startDate)

    companion object {
        const val DAYS_PER_WEEK = 7

        /** The training week that [date] falls into. */
        fun containing(date: LocalDate): TrainingWeek =
            TrainingWeek(date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))
    }
}
