package de.miangohar.overload.domain.logic

import de.miangohar.overload.domain.model.MuscleGroup
import de.miangohar.overload.domain.model.SetEntry
import de.miangohar.overload.domain.model.TrainingGoal
import de.miangohar.overload.domain.model.TrainingPhase
import de.miangohar.overload.domain.model.TrainingWeek
import java.time.LocalDate
import kotlin.math.roundToInt

/** A goal and a matching history of entries, generated together. */
data class SeedData(
    val goal: TrainingGoal,
    val entries: List<SetEntry>,
)

/**
 * Generates a plausible training history for demonstrations and
 * screenshots, ending in the week containing the given date and covering
 * every muscle group across a fixed six day split.
 *
 * One week in the middle of the history is deliberately trained at a
 * fraction of normal volume, so that [DeloadAdvisor] has a genuine deload
 * week to have reacted to, and the accumulation weeks that follow it are
 * timed to match [TrainingGoal.blockLengthWeeks], so that a deload is due
 * again by the most recent week. Pure and deterministic, so its output can
 * be asserted on in a test rather than only read off a device.
 */
object SeedDataGenerator {

    private const val BASELINE_WEEKS = 2
    private const val POST_DELOAD_WEEKS = 4

    /** Comfortably below [DeloadAdvisor.DELOAD_VOLUME_RATIO], so the generated week is detected as one. */
    private const val DELOAD_SCALE = 0.3

    private data class ExerciseTemplate(
        val name: String,
        val primary: MuscleGroup,
        val secondary: Set<MuscleGroup> = emptySet(),
        val sets: Int,
        val reps: Int,
        val weightKg: Double,
        val dayOfWeek: Int,
    )

    private val WEEKLY_TEMPLATE = listOf(
        ExerciseTemplate("Barbell Back Squat", MuscleGroup.QUADS, setOf(MuscleGroup.GLUTES), 5, 6, 80.0, 0),
        ExerciseTemplate("Romanian Deadlift", MuscleGroup.HAMSTRINGS, setOf(MuscleGroup.GLUTES), 4, 8, 70.0, 0),
        ExerciseTemplate("Standing Calf Raise", MuscleGroup.CALVES, sets = 5, reps = 15, weightKg = 60.0, dayOfWeek = 0),

        ExerciseTemplate("Flat Barbell Bench Press", MuscleGroup.CHEST, setOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS_SIDE), 5, 8, 60.0, 1),
        ExerciseTemplate("Seated Overhead Press", MuscleGroup.SHOULDERS_SIDE, setOf(MuscleGroup.TRICEPS), 4, 10, 35.0, 1),
        ExerciseTemplate("Triceps Rope Pushdown", MuscleGroup.TRICEPS, sets = 4, reps = 12, weightKg = 25.0, dayOfWeek = 1),

        ExerciseTemplate("Pull Up", MuscleGroup.BACK, setOf(MuscleGroup.BICEPS), 5, 8, 0.0, 2),
        ExerciseTemplate("Seated Cable Row", MuscleGroup.BACK, setOf(MuscleGroup.BICEPS), 4, 10, 55.0, 2),
        ExerciseTemplate("Dumbbell Bicep Curl", MuscleGroup.BICEPS, sets = 4, reps = 12, weightKg = 14.0, dayOfWeek = 2),

        ExerciseTemplate("Leg Press", MuscleGroup.QUADS, setOf(MuscleGroup.GLUTES), 5, 10, 140.0, 3),
        ExerciseTemplate("Seated Leg Curl", MuscleGroup.HAMSTRINGS, sets = 4, reps = 12, weightKg = 40.0, dayOfWeek = 3),
        ExerciseTemplate("Hanging Leg Raise", MuscleGroup.ABS, sets = 4, reps = 15, weightKg = 0.0, dayOfWeek = 3),

        ExerciseTemplate("Incline Dumbbell Press", MuscleGroup.CHEST, setOf(MuscleGroup.SHOULDERS_SIDE, MuscleGroup.TRICEPS), 4, 10, 24.0, 4),
        ExerciseTemplate("Cable Lateral Raise", MuscleGroup.SHOULDERS_SIDE, sets = 5, reps = 15, weightKg = 7.0, dayOfWeek = 4),
        ExerciseTemplate("Face Pull", MuscleGroup.SHOULDERS_REAR, setOf(MuscleGroup.BACK), 5, 15, 20.0, 4),

        ExerciseTemplate("Lat Pulldown", MuscleGroup.BACK, setOf(MuscleGroup.BICEPS), 4, 10, 55.0, 5),
        ExerciseTemplate("Hammer Curl", MuscleGroup.BICEPS, setOf(MuscleGroup.FOREARMS), 4, 12, 12.0, 5),
        ExerciseTemplate("Wrist Curl", MuscleGroup.FOREARMS, sets = 4, reps = 15, weightKg = 15.0, dayOfWeek = 5),
    )

    fun generate(today: LocalDate = LocalDate.now()): SeedData {
        val currentWeek = TrainingWeek.containing(today)
        val weeks = generateSequence(currentWeek) { it.previous() }
            .take(BASELINE_WEEKS + 1 + POST_DELOAD_WEEKS)
            .toList()
            .asReversed()

        val entries = weeks.flatMapIndexed { index, week ->
            val scale = if (index == BASELINE_WEEKS) DELOAD_SCALE else 1.0
            WEEKLY_TEMPLATE.map { template ->
                SetEntry(
                    date = week.startDate.plusDays(template.dayOfWeek.toLong()),
                    exerciseName = template.name,
                    primaryMuscle = template.primary,
                    secondaryMuscles = template.secondary,
                    sets = (template.sets * scale).roundToInt().coerceAtLeast(1),
                    reps = template.reps,
                    weightKg = template.weightKg,
                    rir = 2,
                )
            }
        }

        val goal = TrainingGoal(
            phase = TrainingPhase.BULK,
            blockLengthWeeks = POST_DELOAD_WEEKS,
            targets = DefaultTargets.forPhase(TrainingPhase.BULK),
        )

        return SeedData(goal, entries)
    }
}
