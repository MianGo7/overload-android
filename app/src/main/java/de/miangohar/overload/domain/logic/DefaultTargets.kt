package de.miangohar.overload.domain.logic

import de.miangohar.overload.domain.model.MuscleGroup
import de.miangohar.overload.domain.model.TrainingPhase
import de.miangohar.overload.domain.model.VolumeTarget
import kotlin.math.roundToInt

/**
 * Suggested starting ranges so that a new user is not faced with an empty form.
 *
 * The numbers are a conventional starting point and are meant to be edited.
 * A maintenance or cut phase shifts the same ranges downwards, which mirrors
 * the practice of holding volume near the lower end while in a deficit.
 */
object DefaultTargets {

    private val ACCUMULATION_RANGES: Map<MuscleGroup, IntRange> = mapOf(
        MuscleGroup.CHEST to 10..20,
        MuscleGroup.BACK to 12..22,
        MuscleGroup.SHOULDERS_SIDE to 8..20,
        MuscleGroup.SHOULDERS_REAR to 6..16,
        MuscleGroup.BICEPS to 8..18,
        MuscleGroup.TRICEPS to 8..18,
        MuscleGroup.QUADS to 10..20,
        MuscleGroup.HAMSTRINGS to 8..16,
        MuscleGroup.GLUTES to 6..16,
        MuscleGroup.CALVES to 8..16,
        MuscleGroup.ABS to 6..16,
        MuscleGroup.FOREARMS to 4..12,
    )

    fun forPhase(phase: TrainingPhase): List<VolumeTarget> {
        val factor = when (phase) {
            TrainingPhase.BULK -> 1.0
            TrainingPhase.MAINTENANCE -> 0.8
            TrainingPhase.CUT -> 0.6
        }
        return ACCUMULATION_RANGES.map { (muscleGroup, range) ->
            VolumeTarget(
                muscleGroup = muscleGroup,
                minSets = scale(range.first, factor),
                maxSets = scale(range.last, factor),
            )
        }
    }

    private fun scale(sets: Int, factor: Double): Int = (sets * factor).roundToInt()
}
