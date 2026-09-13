package de.miangohar.overload.domain

import de.miangohar.overload.domain.model.MuscleGroup
import de.miangohar.overload.domain.model.SetEntry
import de.miangohar.overload.domain.model.TrainingGoal
import de.miangohar.overload.domain.model.TrainingPhase
import de.miangohar.overload.domain.model.TrainingWeek
import de.miangohar.overload.domain.model.VolumeTarget
import java.time.LocalDate

/**
 * Shared fixtures for the domain tests. Keeping them in one place keeps the
 * individual test methods focused on the rule they verify.
 */
internal object TestFixtures {

    /** A Monday, used as the reference week start in all domain tests. */
    val MONDAY: LocalDate = LocalDate.of(2026, 1, 5)

    val WEEK: TrainingWeek = TrainingWeek(MONDAY)

    fun entry(
        date: LocalDate = MONDAY,
        exerciseName: String = "Bench Press",
        primary: MuscleGroup = MuscleGroup.CHEST,
        secondary: Set<MuscleGroup> = emptySet(),
        sets: Int = 3,
        reps: Int = 10,
        weightKg: Double = 60.0,
        rir: Int? = 2,
    ): SetEntry = SetEntry(
        date = date,
        exerciseName = exerciseName,
        primaryMuscle = primary,
        secondaryMuscles = secondary,
        sets = sets,
        reps = reps,
        weightKg = weightKg,
        rir = rir,
    )

    fun goal(
        vararg targets: VolumeTarget,
        phase: TrainingPhase = TrainingPhase.BULK,
        blockLengthWeeks: Int = TrainingGoal.DEFAULT_BLOCK_LENGTH_WEEKS,
    ): TrainingGoal = TrainingGoal(
        phase = phase,
        blockLengthWeeks = blockLengthWeeks,
        targets = targets.toList(),
    )

    fun target(
        muscleGroup: MuscleGroup,
        minSets: Int,
        maxSets: Int = minSets + 6,
    ): VolumeTarget = VolumeTarget(muscleGroup, minSets, maxSets)
}
