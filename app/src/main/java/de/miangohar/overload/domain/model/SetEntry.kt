package de.miangohar.overload.domain.model

import java.time.LocalDate

/**
 * One logged block of straight sets for a single exercise on a single day.
 *
 * A set counts fully towards the primary muscle group and, following common
 * hypertrophy practice, only fractionally towards the muscles it trains
 * indirectly. The fraction itself is defined in
 * [de.miangohar.overload.domain.logic.VolumeCalculator].
 *
 * @param id database identifier, [NO_ID] while the entry is not persisted yet
 * @param sets number of hard sets performed
 * @param reps repetitions per set, used for tonnage rather than for volume
 * @param weightKg load per set in kilograms
 * @param rir reps in reserve, null when the user did not record it
 */
data class SetEntry(
    val id: Long = NO_ID,
    val date: LocalDate,
    val exerciseName: String,
    val primaryMuscle: MuscleGroup,
    val secondaryMuscles: Set<MuscleGroup> = emptySet(),
    val sets: Int,
    val reps: Int,
    val weightKg: Double,
    val rir: Int? = null,
) {
    init {
        require(exerciseName.isNotBlank()) { "exerciseName must not be blank" }
        require(sets > 0) { "sets must be greater than zero" }
        require(reps > 0) { "reps must be greater than zero" }
        require(weightKg >= 0.0) { "weightKg must not be negative" }
        require(rir == null || rir >= 0) { "rir must not be negative" }
        require(primaryMuscle !in secondaryMuscles) {
            "primaryMuscle must not also be listed as a secondary muscle"
        }
    }

    /** Tonnage of this entry in kilograms, a common secondary progress metric. */
    val volumeLoadKg: Double
        get() = sets * reps * weightKg

    companion object {
        const val NO_ID = 0L
    }
}
