package de.miangohar.overload.domain.logic

import de.miangohar.overload.domain.model.TrainingGoal
import de.miangohar.overload.domain.model.WeeklyTotal

/** Why the advisor arrived at its recommendation. */
enum class DeloadReason {
    /** Not enough logged history to judge anything. */
    NO_HISTORY,

    /** The most recent week already looks like a deload. */
    DELOAD_IN_PROGRESS,

    /** Still inside the configured accumulation block. */
    WITHIN_BLOCK,

    /** The configured number of accumulation weeks has been reached. */
    BLOCK_COMPLETE,
}

/**
 * @param accumulationWeeks consecutive non deload weeks counted back from the
 *   most recent logged week
 */
data class DeloadRecommendation(
    val isDue: Boolean,
    val accumulationWeeks: Int,
    val reason: DeloadReason,
)

/**
 * Decides whether a deload week is due.
 *
 * A week counts as a deload when its total volume drops to at most half of the
 * trailing average of the weeks before it, which matches the common practice of
 * halving sets while keeping the load, see ADR-0017. The advisor never looks
 * at calendar gaps, only at logged volume, so a week without any entry is
 * treated as a light week rather than as missing data.
 */
object DeloadAdvisor {

    /** Share of the trailing average at or below which a week counts as a deload, ADR-0017. */
    const val DELOAD_VOLUME_RATIO = 0.5

    /** Number of preceding weeks the trailing average is taken over. */
    const val TRAILING_WEEKS = 3

    /**
     * @param totals weekly totals in any order, they are sorted internally
     * @param goal supplies the configured block length
     */
    fun recommend(totals: List<WeeklyTotal>, goal: TrainingGoal): DeloadRecommendation {
        val ordered = totals.sortedBy { it.week }
        if (ordered.isEmpty()) {
            return DeloadRecommendation(
                isDue = false,
                accumulationWeeks = 0,
                reason = DeloadReason.NO_HISTORY,
            )
        }

        if (isDeloadWeek(ordered, ordered.lastIndex)) {
            return DeloadRecommendation(
                isDue = false,
                accumulationWeeks = 0,
                reason = DeloadReason.DELOAD_IN_PROGRESS,
            )
        }

        var accumulationWeeks = 0
        for (index in ordered.indices.reversed()) {
            if (isDeloadWeek(ordered, index)) break
            accumulationWeeks++
        }

        val isDue = accumulationWeeks >= goal.blockLengthWeeks
        return DeloadRecommendation(
            isDue = isDue,
            accumulationWeeks = accumulationWeeks,
            reason = if (isDue) DeloadReason.BLOCK_COMPLETE else DeloadReason.WITHIN_BLOCK,
        )
    }

    /**
     * Whether the week at [index] of the chronologically ordered [totals] looks
     * like a deload. The first weeks of the history have no trailing average to
     * compare against and therefore never count as a deload.
     */
    fun isDeloadWeek(totals: List<WeeklyTotal>, index: Int): Boolean {
        require(index in totals.indices) { "index is outside the totals list" }
        val trailing = totals.subList(maxOf(0, index - TRAILING_WEEKS), index)
        if (trailing.isEmpty()) return false
        val average = trailing.sumOf { it.totalSets } / trailing.size
        if (average <= 0.0) return false
        return totals[index].totalSets <= average * DELOAD_VOLUME_RATIO
    }
}
