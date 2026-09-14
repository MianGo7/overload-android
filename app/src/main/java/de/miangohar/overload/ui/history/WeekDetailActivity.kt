package de.miangohar.overload.ui.history

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.miangohar.overload.ui.theme.OverloadTheme
import java.time.LocalDate

/**
 * Detail screen for one past week.
 *
 * The week is passed in as an intent extra, which keeps the two activities
 * loosely coupled: the detail screen only needs the start date, not the list
 * state of the caller.
 */
class WeekDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val weekStartEpochDay = intent.getLongExtra(
            EXTRA_WEEK_START_EPOCH_DAY,
            LocalDate.now().toEpochDay(),
        )

        setContent {
            OverloadTheme {
                val viewModel: WeekDetailViewModel = viewModel(
                    factory = WeekDetailViewModel.factory(weekStartEpochDay),
                )
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                WeekDetailScreen(
                    state = state,
                    onDeleteRequested = viewModel::onDeleteRequested,
                    onBack = { finish() },
                )
            }
        }
    }

    companion object {
        const val EXTRA_WEEK_START_EPOCH_DAY = "de.miangohar.overload.extra.WEEK_START_EPOCH_DAY"
    }
}
