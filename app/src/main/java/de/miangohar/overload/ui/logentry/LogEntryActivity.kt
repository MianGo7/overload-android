package de.miangohar.overload.ui.logentry

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.miangohar.overload.ui.theme.OverloadTheme

/** Screen for logging one block of sets. */
class LogEntryActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OverloadTheme {
                val viewModel: LogEntryViewModel = viewModel(factory = LogEntryViewModel.Factory)
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(state.isSaved) {
                    if (state.isSaved) finish()
                }

                LogEntryScreen(
                    state = state,
                    onExerciseNameChanged = viewModel::onExerciseNameChanged,
                    onPrimaryMuscleSelected = viewModel::onPrimaryMuscleSelected,
                    onSecondaryMuscleToggled = viewModel::onSecondaryMuscleToggled,
                    onSetsChanged = viewModel::onSetsChanged,
                    onRepsChanged = viewModel::onRepsChanged,
                    onWeightChanged = viewModel::onWeightChanged,
                    onRirChanged = viewModel::onRirChanged,
                    onDateShifted = viewModel::onDateShifted,
                    onSaveRequested = viewModel::onSaveRequested,
                    onBack = { finish() },
                )
            }
        }
    }
}
