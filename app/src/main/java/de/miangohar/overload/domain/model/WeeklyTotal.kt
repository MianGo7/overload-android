package de.miangohar.overload.domain.model

/** Total accumulated sets of one training week, used for deload detection. */
data class WeeklyTotal(
    val week: TrainingWeek,
    val totalSets: Double,
)
