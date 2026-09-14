package de.miangohar.overload.ui.history

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.miangohar.overload.ui.theme.OverloadTheme

/** Lists past training weeks and opens the detail screen for one of them. */
class HistoryActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OverloadTheme {
                val viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory)
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                HistoryScreen(
                    state = state,
                    onTrendMuscleGroupSelected = viewModel::onTrendMuscleGroupSelected,
                    onOpenWeek = { week ->
                        val intent = Intent(this, WeekDetailActivity::class.java)
                            .putExtra(
                                WeekDetailActivity.EXTRA_WEEK_START_EPOCH_DAY,
                                week.startDate.toEpochDay(),
                            )
                        startActivity(intent)
                    },
                    onBack = { finish() },
                )
            }
        }
    }
}
