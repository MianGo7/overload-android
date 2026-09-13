package de.miangohar.overload.ui.common

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.miangohar.overload.R
import de.miangohar.overload.domain.model.MuscleGroup
import de.miangohar.overload.domain.model.TrainingPhase

/**
 * Maps domain enums to string resources.
 *
 * The mapping lives in the UI layer on purpose. The domain model carries no
 * display text, which is what keeps it free of Android dependencies and
 * testable on the JVM.
 */
@StringRes
fun MuscleGroup.labelRes(): Int = when (this) {
    MuscleGroup.CHEST -> R.string.muscle_chest
    MuscleGroup.BACK -> R.string.muscle_back
    MuscleGroup.SHOULDERS_SIDE -> R.string.muscle_shoulders_side
    MuscleGroup.SHOULDERS_REAR -> R.string.muscle_shoulders_rear
    MuscleGroup.BICEPS -> R.string.muscle_biceps
    MuscleGroup.TRICEPS -> R.string.muscle_triceps
    MuscleGroup.QUADS -> R.string.muscle_quads
    MuscleGroup.HAMSTRINGS -> R.string.muscle_hamstrings
    MuscleGroup.GLUTES -> R.string.muscle_glutes
    MuscleGroup.CALVES -> R.string.muscle_calves
    MuscleGroup.ABS -> R.string.muscle_abs
    MuscleGroup.FOREARMS -> R.string.muscle_forearms
}

@StringRes
fun TrainingPhase.labelRes(): Int = when (this) {
    TrainingPhase.BULK -> R.string.phase_bulk
    TrainingPhase.MAINTENANCE -> R.string.phase_maintenance
    TrainingPhase.CUT -> R.string.phase_cut
}

@Composable
fun MuscleGroup.label(): String = stringResource(labelRes())

@Composable
fun TrainingPhase.label(): String = stringResource(labelRes())
