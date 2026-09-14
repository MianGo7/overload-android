package de.miangohar.overload.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.miangohar.overload.OverloadApplication
import de.miangohar.overload.domain.logic.DeloadAdvisor
import de.miangohar.overload.domain.logic.DeloadRecommendation
import de.miangohar.overload.domain.logic.ProgressEvaluator
import de.miangohar.overload.domain.logic.SeedDataGenerator
import de.miangohar.overload.domain.logic.VolumeCalculator
import de.miangohar.overload.domain.model.TrainingGoal
import de.miangohar.overload.domain.model.TrainingWeek
import de.miangohar.overload.domain.model.WeeklyProgress
import de.miangohar.overload.domain.repository.GoalRepository
import de.miangohar.overload.domain.repository.SetEntryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

/** Everything the dashboard needs to render one frame. */
data class DashboardUiState(
    val week: TrainingWeek,
    val progress: WeeklyProgress?,
    val deload: DeloadRecommendation?,
    val blockLengthWeeks: Int,
    val hasGoal: Boolean,
    val hasEntries: Boolean,
    val isLoading: Boolean,
) {
    companion object {
        fun loading(week: TrainingWeek) = DashboardUiState(
            week = week,
            progress = null,
            deload = null,
            blockLengthWeeks = TrainingGoal.DEFAULT_BLOCK_LENGTH_WEEKS,
            hasGoal = false,
            hasEntries = false,
            isLoading = true,
        )
    }
}

/**
 * Combines logged entries and the stored goal into the weekly evaluation.
 *
 * The view model holds no rules of its own. It only decides which week is
 * current and hands the work to the domain layer, which keeps the rules
 * testable without an Android runtime.
 */
class DashboardViewModel(
    private val setEntryRepository: SetEntryRepository,
    private val goalRepository: GoalRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ViewModel() {

    // A week's dashboard has nothing that naturally re-emits at midnight, so
    // a resume is used to notice a rolled over week; see onResumed().
    private val resumeSignal = MutableStateFlow(0)

    val uiState: StateFlow<DashboardUiState> = combine(
        setEntryRepository.observeAll(),
        goalRepository.observeGoal(),
        resumeSignal,
    ) { entries, goal, _ ->
        val week = TrainingWeek.containing(LocalDate.now(clock))
        DashboardUiState(
            week = week,
            progress = ProgressEvaluator.evaluate(week, entries, goal),
            deload = DeloadAdvisor.recommend(VolumeCalculator.weeklyTotals(entries), goal),
            blockLengthWeeks = goal.blockLengthWeeks,
            hasGoal = goal.targets.isNotEmpty(),
            hasEntries = entries.isNotEmpty(),
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = DashboardUiState.loading(TrainingWeek.containing(LocalDate.now(clock))),
    )

    /**
     * Fills the database with a plausible training history for demonstrations
     * and screenshots. Only ever called from a debug build, see
     * [de.miangohar.overload.BuildConfig.DEBUG] at the call site.
     */
    fun onSeedRequested() {
        val seedData = SeedDataGenerator.generate(LocalDate.now(clock))
        viewModelScope.launch {
            goalRepository.saveGoal(seedData.goal)
            seedData.entries.forEach { entry -> setEntryRepository.add(entry) }
        }
    }

    /**
     * Re-evaluates the current week. Neither repository flow emits on its
     * own at midnight, so a dashboard left open across the boundary would
     * otherwise keep showing the week it opened on; the activity calls this
     * on every resume, which covers reopening the app spanning midnight.
     * A dashboard kept continuously in the foreground, never resumed,
     * through the exact rollover is a residual, accepted limitation, see
     * the dev journal.
     */
    fun onResumed() {
        resumeSignal.update { it + 1 }
    }

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5_000L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as OverloadApplication
                DashboardViewModel(
                    setEntryRepository = application.container.setEntryRepository,
                    goalRepository = application.container.goalRepository,
                )
            }
        }
    }
}
