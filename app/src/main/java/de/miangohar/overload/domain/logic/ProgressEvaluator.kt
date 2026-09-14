package de.miangohar.overload.domain.logic

import de.miangohar.overload.domain.model.MuscleGroup
import de.miangohar.overload.domain.model.MuscleVolume
import de.miangohar.overload.domain.model.SetEntry
import de.miangohar.overload.domain.model.TrainingGoal
import de.miangohar.overload.domain.model.TrainingWeek
import de.miangohar.overload.domain.model.VolumeStatus
import de.miangohar.overload.domain.model.VolumeTarget
import de.miangohar.overload.domain.model.WeeklyProgress

/**
 * Compares accumulated weekly volume against the goal the user configured.
 *
 * This is the evaluation step the task description asks for. It is kept
 * separate from [VolumeCalculator] so that counting rules and judgement rules
 * can be changed and tested independently of each other.
 */
object ProgressEvaluator {

    /** Evaluates one training week against the user goal. */
    fun evaluate(
        week: TrainingWeek,
        entries: List<SetEntry>,
        goal: TrainingGoal,
    ): WeeklyProgress {
        val volumeByMuscle = VolumeCalculator.weeklyVolume(entries, week)
        val muscleGroups = (goal.targets.map { it.muscleGroup } + volumeByMuscle.keys)
            .distinct()
            .sortedBy { it.ordinal }

        val muscleVolumes = muscleGroups.map { muscleGroup ->
            val completedSets = volumeByMuscle[muscleGroup] ?: 0.0
            val target = goal.targetFor(muscleGroup)
            MuscleVolume(
                muscleGroup = muscleGroup,
                completedSets = completedSets,
                target = target,
                status = statusFor(completedSets, target),
                completionRatio = completionRatio(completedSets, target),
            )
        }

        val targeted = muscleVolumes.filter { it.target != null }
        val overallCompletion = if (targeted.isEmpty()) {
            0.0
        } else {
            targeted.sumOf { it.completionRatio } / targeted.size
        }

        return WeeklyProgress(
            week = week,
            muscleVolumes = muscleVolumes,
            overallCompletion = overallCompletion,
            totalSets = VolumeCalculator.totalSets(entries, week),
            volumeLoadKg = VolumeCalculator.volumeLoadKg(entries, week),
        )
    }

    /** Where the accumulated sets sit relative to the target range. */
    fun statusFor(completedSets: Double, target: VolumeTarget?): VolumeStatus = when {
        target == null -> VolumeStatus.UNTARGETED
        completedSets < target.minSets -> VolumeStatus.BELOW_TARGET
        completedSets > target.maxSets -> VolumeStatus.ABOVE_TARGET
        else -> VolumeStatus.IN_TARGET
    }

    /**
     * Progress towards the lower bound of the range, coerced into 0.0 to 1.0.
     * The lower bound is used as the goal line because it is the point from
     * which the week counts as productive.
     */
    fun completionRatio(completedSets: Double, target: VolumeTarget?): Double = when {
        target == null -> 0.0
        target.minSets == 0 -> 1.0
        else -> (completedSets / target.minSets).coerceIn(0.0, 1.0)
    }

    /**
     * The range to draw behind a volume trend. A single muscle group uses its
     * own target, null when it has none. With no muscle group selected the
     * range sums every tracked target, since there is no single figure for
     * "overall" hard sets otherwise, null when nothing is tracked at all.
     */
    fun targetBand(goal: TrainingGoal, muscleGroup: MuscleGroup?): ClosedFloatingPointRange<Double>? {
        if (muscleGroup != null) {
            val target = goal.targetFor(muscleGroup) ?: return null
            return target.minSets.toDouble()..target.maxSets.toDouble()
        }
        if (goal.targets.isEmpty()) return null
        return goal.targets.sumOf { it.minSets }.toDouble()..goal.targets.sumOf { it.maxSets }.toDouble()
    }
}
