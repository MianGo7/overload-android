package de.miangohar.overload.domain.model

/**
 * The muscle groups that weekly volume is tracked for.
 *
 * The enum deliberately carries no display text. User facing labels live in
 * string resources and are resolved in the UI layer, which keeps this layer
 * free of Android dependencies and unit testable on the JVM.
 */
enum class MuscleGroup {
    CHEST,
    BACK,
    SHOULDERS_SIDE,
    SHOULDERS_REAR,
    BICEPS,
    TRICEPS,
    QUADS,
    HAMSTRINGS,
    GLUTES,
    CALVES,
    ABS,
    FOREARMS,
}
