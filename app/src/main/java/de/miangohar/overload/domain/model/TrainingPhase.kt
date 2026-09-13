package de.miangohar.overload.domain.model

/**
 * The mesocycle phase the user is currently in. The phase does not change how
 * volume is counted, it only frames the targets the user sets for themselves.
 */
enum class TrainingPhase {
    BULK,
    MAINTENANCE,
    CUT,
}
