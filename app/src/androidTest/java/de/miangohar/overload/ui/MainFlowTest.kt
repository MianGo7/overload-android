package de.miangohar.overload.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.miangohar.overload.R
import de.miangohar.overload.data.local.AppDatabase
import de.miangohar.overload.data.repository.RoomGoalRepository
import de.miangohar.overload.data.repository.RoomSetEntryRepository
import de.miangohar.overload.domain.model.MuscleGroup
import de.miangohar.overload.domain.model.VolumeStatus
import de.miangohar.overload.ui.common.formatSets
import de.miangohar.overload.ui.dashboard.DashboardScreen
import de.miangohar.overload.ui.dashboard.DashboardViewModel
import de.miangohar.overload.ui.goal.GoalScreen
import de.miangohar.overload.ui.goal.GoalViewModel
import de.miangohar.overload.ui.logentry.LogEntryScreen
import de.miangohar.overload.ui.logentry.LogEntryViewModel
import de.miangohar.overload.ui.theme.OverloadTheme
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private enum class Screen { GOAL, LOG_ENTRY, DASHBOARD }

/**
 * Covers the path a user actually takes: set a target range for one muscle
 * group, log a set of that exercise, and see the dashboard report the
 * correct status for it. Runs against an in memory Room database and the
 * real view models rather than fakes, so the repositories, the domain rules
 * they call and the screens are all exercised together, only the database
 * file itself is not the one shipped to a device.
 *
 * Uses the deprecated `createComposeRule()` rather than its `v2` successor
 * deliberately: `v2` runs composition on a `StandardTestDispatcher`, which
 * queues coroutines instead of running them, so the real view models' own
 * `viewModelScope.launch` calls, mixed with genuine asynchronous Room
 * queries, would need their own explicit clock driving throughout this
 * test. That rework was judged a real risk to get subtly wrong versus the
 * cost of one deprecation warning against a still fully supported API.
 */
@RunWith(AndroidJUnit4::class)
class MainFlowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var goalViewModel: GoalViewModel
    private lateinit var logEntryViewModel: LogEntryViewModel
    private lateinit var dashboardViewModel: DashboardViewModel

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val goalRepository = RoomGoalRepository(database, database.goalDao())
        val setEntryRepository = RoomSetEntryRepository(database.setEntryDao())
        goalViewModel = GoalViewModel(goalRepository)
        logEntryViewModel = LogEntryViewModel(setEntryRepository)
        dashboardViewModel = DashboardViewModel(setEntryRepository, goalRepository)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun settingAGoalAndLoggingAnEntryUpdatesTheDashboard() {
        var screen by mutableStateOf(Screen.GOAL)

        composeTestRule.setContent {
            OverloadTheme {
                when (screen) {
                    Screen.GOAL -> {
                        val state by goalViewModel.uiState.collectAsStateWithLifecycle()
                        GoalScreen(
                            state = state,
                            onPhaseSelected = goalViewModel::onPhaseSelected,
                            onBlockLengthChanged = goalViewModel::onBlockLengthChanged,
                            onMinChanged = goalViewModel::onMinChanged,
                            onMaxChanged = goalViewModel::onMaxChanged,
                            onPrefillRequested = goalViewModel::onPrefillRequested,
                            onReminderTimeChanged = goalViewModel::onReminderTimeChanged,
                            onSaveRequested = goalViewModel::onSaveRequested,
                            onBack = {},
                        )
                    }

                    Screen.LOG_ENTRY -> {
                        val state by logEntryViewModel.uiState.collectAsStateWithLifecycle()
                        LogEntryScreen(
                            state = state,
                            onExerciseNameChanged = logEntryViewModel::onExerciseNameChanged,
                            onPrimaryMuscleSelected = logEntryViewModel::onPrimaryMuscleSelected,
                            onSecondaryMuscleToggled = logEntryViewModel::onSecondaryMuscleToggled,
                            onSetsChanged = logEntryViewModel::onSetsChanged,
                            onRepsChanged = logEntryViewModel::onRepsChanged,
                            onWeightChanged = logEntryViewModel::onWeightChanged,
                            onRirChanged = logEntryViewModel::onRirChanged,
                            onDateShifted = logEntryViewModel::onDateShifted,
                            onSaveRequested = logEntryViewModel::onSaveRequested,
                            onBack = {},
                        )
                    }

                    Screen.DASHBOARD -> {
                        val state by dashboardViewModel.uiState.collectAsStateWithLifecycle()
                        DashboardScreen(
                            state = state,
                            onLogSets = {},
                            onEditGoal = {},
                            onOpenHistory = {},
                        )
                    }
                }
            }
        }

        // Set a narrow range for chest, so three logged sets land inside it,
        // rather than the default empty form which tracks nothing.
        val saveGoal = context.getString(R.string.goal_action_save)
        val minLabel = context.getString(R.string.goal_min)
        val maxLabel = context.getString(R.string.goal_max)
        composeTestRule.waitUntil(timeoutMillis = WAIT_TIMEOUT_MILLIS) {
            composeTestRule.onAllNodesWithText(saveGoal).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onAllNodesWithText(minLabel)[0].performTextInput("1")
        composeTestRule.onAllNodesWithText(maxLabel)[0].performTextInput("5")
        composeTestRule.waitForIdle()
        val chestRow = goalViewModel.uiState.value.rows.single { it.muscleGroup == MuscleGroup.CHEST }
        assertEquals("1", chestRow.minSets)
        assertEquals("5", chestRow.maxSets)
        composeTestRule.onNodeWithText(saveGoal).performScrollTo().performClick()
        composeTestRule.waitUntil(timeoutMillis = WAIT_TIMEOUT_MILLIS) {
            goalViewModel.uiState.value.isSaved
        }

        // Log three sets of chest work, the default primary muscle and set
        // count, only the exercise name and weight are required to save.
        screen = Screen.LOG_ENTRY
        composeTestRule.onNodeWithText(context.getString(R.string.log_exercise))
            .performTextInput("Bench Press")
        composeTestRule.onNodeWithText(context.getString(R.string.log_weight))
            .performScrollTo()
            .performTextInput("60")
        composeTestRule.onNodeWithText(context.getString(R.string.log_action_save))
            .performScrollTo()
            .performClick()
        composeTestRule.waitUntil(timeoutMillis = WAIT_TIMEOUT_MILLIS) {
            logEntryViewModel.uiState.value.isSaved
        }

        // The dashboard's uiState is a stateIn flow that only starts
        // collecting once something subscribes to it, so it has to be on
        // screen before its state reflects the two writes above.
        screen = Screen.DASHBOARD
        composeTestRule.waitUntil(timeoutMillis = WAIT_TIMEOUT_MILLIS) {
            dashboardViewModel.uiState.value.progress
                ?.muscleVolumes
                ?.any { it.muscleGroup == MuscleGroup.CHEST && it.completedSets == 3.0 } == true
        }
        val chestVolume = dashboardViewModel.uiState.value.progress!!.muscleVolumes
            .single { it.muscleGroup == MuscleGroup.CHEST }
        assertEquals(VolumeStatus.IN_TARGET, chestVolume.status)

        composeTestRule.onNodeWithText(context.getString(R.string.status_in_target)).assertExists()
        val chestTarget = chestVolume.target!!
        val expectedSummary = context.getString(
            R.string.volume_sets_of_range,
            formatSets(chestVolume.completedSets),
            chestTarget.minSets,
            chestTarget.maxSets,
        )
        composeTestRule.onNodeWithText(expectedSummary).assertExists()
    }

    private companion object {
        const val WAIT_TIMEOUT_MILLIS = 5_000L
    }
}
