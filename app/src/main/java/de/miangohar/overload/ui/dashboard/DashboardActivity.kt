package de.miangohar.overload.ui.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.miangohar.overload.ui.goal.GoalActivity
import de.miangohar.overload.ui.history.HistoryActivity
import de.miangohar.overload.ui.logentry.LogEntryActivity
import de.miangohar.overload.ui.theme.OverloadTheme

/**
 * Launcher activity showing the current training week.
 *
 * The app uses one activity per screen and explicit intents to move between
 * them, which mirrors the navigation model of the course material.
 */
class DashboardActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OverloadTheme {
                val viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) viewModel.onResumed()
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                }

                DashboardScreen(
                    state = state,
                    onLogSets = { startActivity(Intent(this, LogEntryActivity::class.java)) },
                    onEditGoal = { startActivity(Intent(this, GoalActivity::class.java)) },
                    onOpenHistory = { startActivity(Intent(this, HistoryActivity::class.java)) },
                    onSeedDemoData = viewModel::onSeedRequested,
                )
            }
        }
    }
}
