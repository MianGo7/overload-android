package de.miangohar.overload.domain.logic

import de.miangohar.overload.domain.model.MuscleGroup
import de.miangohar.overload.domain.model.SetEntry
import de.miangohar.overload.domain.model.TrainingWeek
import de.miangohar.overload.domain.model.WeeklyTotal

/**
 * Turns logged entries into accumulated weekly volume.
 *
 * A hard set counts once for the muscle group the exercise trains directly and
 * only half for the groups it trains indirectly. Counting indirect work at a
 * fraction is a common convention in hypertrophy programming and avoids both
 * ignoring it and double counting it.
 *
 * The object holds no state and touches no Android API, so every rule in here
 * is covered by plain JVM unit tests.
 */
object VolumeCalculator {

    const val PRIMARY_SET_CREDIT = 1.0
    const val SECONDARY_SET_CREDIT = 0.5

    /**
     * Accumulated fractional sets per muscle group for the given week.
     * Muscle groups without any logged work are absent from the result.
     */
    fun weeklyVolume(entries: List<SetEntry>, week: TrainingWeek): Map<MuscleGroup, Double> {
        val volume = linkedMapOf<MuscleGroup, Double>()
        entriesIn(entries, week).forEach { entry ->
            volume.merge(entry.primaryMuscle, entry.sets * PRIMARY_SET_CREDIT, Double::plus)
            entry.secondaryMuscles.forEach { muscle ->
                volume.merge(muscle, entry.sets * SECONDARY_SET_CREDIT, Double::plus)
            }
        }
        return volume
    }

    /** Sum of all fractional sets of the week across every muscle group. */
    fun totalSets(entries: List<SetEntry>, week: TrainingWeek): Double =
        weeklyVolume(entries, week).values.sum()

    /** Tonnage of the week in kilograms, sets times reps times load. */
    fun volumeLoadKg(entries: List<SetEntry>, week: TrainingWeek): Double =
        entriesIn(entries, week).sumOf { it.volumeLoadKg }

    /** Weekly totals for every week that contains at least one entry, oldest first. */
    fun weeklyTotals(entries: List<SetEntry>): List<WeeklyTotal> =
        entries
            .groupBy { TrainingWeek.containing(it.date) }
            .toSortedMap()
            .map { (week, weekEntries) ->
                WeeklyTotal(week, totalSets(weekEntries, week))
            }

    /**
     * Weekly totals for the [weekCount] weeks up to and including
     * [currentWeek], oldest first, one entry per week even where nothing was
     * logged, so a trend chart never has to skip a gap in the history.
     *
     * @param muscleGroup a single tracked muscle group, or null for the
     *   total across every muscle group
     */
    fun recentWeeklyTotals(
        entries: List<SetEntry>,
        currentWeek: TrainingWeek,
        weekCount: Int,
        muscleGroup: MuscleGroup? = null,
    ): List<WeeklyTotal> {
        require(weekCount > 0) { "weekCount must be positive" }
        val weeks = generateSequence(currentWeek) { it.previous() }.take(weekCount).toList().asReversed()
        return weeks.map { week ->
            val sets = if (muscleGroup == null) {
                totalSets(entries, week)
            } else {
                weeklyVolume(entries, week)[muscleGroup] ?: 0.0
            }
            WeeklyTotal(week, sets)
        }
    }

    private fun entriesIn(entries: List<SetEntry>, week: TrainingWeek): List<SetEntry> =
        entries.filter { it.date in week }
}
