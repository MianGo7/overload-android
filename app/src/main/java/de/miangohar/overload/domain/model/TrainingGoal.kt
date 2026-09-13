package de.miangohar.overload.domain.model

/**
 * The individual goal the user configures once and adjusts between mesocycles.
 *
 * @param phase the current mesocycle phase
 * @param blockLengthWeeks number of accumulation weeks before a deload is due
 * @param targets weekly set ranges, at most one entry per muscle group
 */
data class TrainingGoal(
    val phase: TrainingPhase,
    val blockLengthWeeks: Int,
    val targets: List<VolumeTarget>,
) {
    init {
        require(blockLengthWeeks in MIN_BLOCK_LENGTH_WEEKS..MAX_BLOCK_LENGTH_WEEKS) {
            "blockLengthWeeks must be between $MIN_BLOCK_LENGTH_WEEKS and $MAX_BLOCK_LENGTH_WEEKS"
        }
        require(targets.distinctBy { it.muscleGroup }.size == targets.size) {
            "targets must not contain the same muscle group twice"
        }
    }

    fun targetFor(muscleGroup: MuscleGroup): VolumeTarget? =
        targets.firstOrNull { it.muscleGroup == muscleGroup }

    companion object {
        const val MIN_BLOCK_LENGTH_WEEKS = 3
        const val MAX_BLOCK_LENGTH_WEEKS = 8
        const val DEFAULT_BLOCK_LENGTH_WEEKS = 5

        /** An empty starting goal, used before the user has configured anything. */
        val EMPTY = TrainingGoal(
            phase = TrainingPhase.MAINTENANCE,
            blockLengthWeeks = DEFAULT_BLOCK_LENGTH_WEEKS,
            targets = emptyList(),
        )
    }
}
