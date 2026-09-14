package de.miangohar.overload.ui.goal

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.miangohar.overload.reminder.ReminderScheduler
import de.miangohar.overload.ui.theme.OverloadTheme

/** Screen for defining the individual weekly goal. */
class GoalActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OverloadTheme {
                val context = LocalContext.current
                val viewModel: GoalViewModel = viewModel(factory = GoalViewModel.Factory)
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                var notificationsBlocked by remember {
                    mutableStateOf(!NotificationManagerCompat.from(context).areNotificationsEnabled())
                }
                val requestPermission = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission(),
                ) { granted -> notificationsBlocked = !granted }

                LaunchedEffect(state.isSaved) {
                    if (state.isSaved) {
                        ReminderScheduler.apply(context, state.reminderTime)
                        finish()
                    }
                }

                GoalScreen(
                    state = state,
                    onPhaseSelected = viewModel::onPhaseSelected,
                    onBlockLengthChanged = viewModel::onBlockLengthChanged,
                    onMinChanged = viewModel::onMinChanged,
                    onMaxChanged = viewModel::onMaxChanged,
                    onPrefillRequested = viewModel::onPrefillRequested,
                    onReminderTimeChanged = { time ->
                        viewModel.onReminderTimeChanged(time)
                        if (time != null &&
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                            PackageManager.PERMISSION_GRANTED
                        ) {
                            requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    onSaveRequested = viewModel::onSaveRequested,
                    onBack = { finish() },
                    notificationsBlocked = notificationsBlocked,
                )
            }
        }
    }
}
