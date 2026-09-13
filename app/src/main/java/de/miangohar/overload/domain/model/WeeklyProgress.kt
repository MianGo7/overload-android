package de.miangohar.overload.domain.model

/** How the volume accumulated for one muscle group relates to its target range. */
enum class VolumeStatus {
    /** No target configured for this muscle group. */
    UNTARGETED,

    /** Below the lower bound of the range. */
    BELOW_TARGET,

    /** Inside the range, including both bounds. */
    IN_TARGET,

    /** Above the upper bound of the range. */
    ABOVE_TARGET,
}

/**
 * Accumulated volume for one muscle group within one training week.
 *
 * @param completedSets fractional set count, secondary work counts partially
 * @param completionRatio [completedSets] divided by the lower target bound,
 *   coerced into 0.0 to 1.0 so that it can drive a progress indicator directly
 */
data class MuscleVolume(
    val muscleGroup: MuscleGroup,
    val completedSets: Double,
    val target: VolumeTarget?,
    val status: VolumeStatus,
    val completionRatio: Double,
)

/**
 * The evaluated result for one training week, which is what the dashboard shows.
 *
 * @param overallCompletion mean completion ratio across all targeted muscle
 *   groups, 0.0 when the user has not configured any target yet
 */
data class WeeklyProgress(
    val week: TrainingWeek,
    val muscleVolumes: List<MuscleVolume>,
    val overallCompletion: Double,
    val totalSets: Double,
    val volumeLoadKg: Double,
) {
    val musclesBelowTarget: List<MuscleVolume>
        get() = muscleVolumes.filter { it.status == VolumeStatus.BELOW_TARGET }

    val musclesAboveTarget: List<MuscleVolume>
        get() = muscleVolumes.filter { it.status == VolumeStatus.ABOVE_TARGET }
}
