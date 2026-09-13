package de.miangohar.overload.ui.goal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.miangohar.overload.ui.theme.OverloadTheme

/** Screen for defining the individual weekly goal. */
class GoalActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OverloadTheme {
                val viewModel: GoalViewModel = viewModel(factory = GoalViewModel.Factory)
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(state.isSaved) {
                    if (state.isSaved) finish()
                }

                GoalScreen(
                    state = state,
                    onPhaseSelected = viewModel::onPhaseSelected,
                    onBlockLengthChanged = viewModel::onBlockLengthChanged,
                    onMinChanged = viewModel::onMinChanged,
                    onMaxChanged = viewModel::onMaxChanged,
                    onPrefillRequested = viewModel::onPrefillRequested,
                    onSaveRequested = viewModel::onSaveRequested,
                    onBack = { finish() },
                )
            }
        }
    }
}
