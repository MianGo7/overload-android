package de.miangohar.overload.domain.model

/**
 * A weekly target range of hard sets for one muscle group.
 *
 * The range models the common practice of training between a minimum effective
 * volume and a maximum recoverable volume rather than hitting a single number.
 *
 * @param minSets lower bound of the weekly range, treated as the goal line
 * @param maxSets upper bound of the weekly range, above which volume is flagged
 */
data class VolumeTarget(
    val muscleGroup: MuscleGroup,
    val minSets: Int,
    val maxSets: Int,
) {
    init {
        require(minSets >= 0) { "minSets must not be negative" }
        require(maxSets >= minSets) { "maxSets must not be smaller than minSets" }
    }
}
