package de.miangohar.overload.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.miangohar.overload.OverloadApplication
import de.miangohar.overload.domain.logic.DeloadAdvisor
import de.miangohar.overload.domain.logic.ProgressEvaluator
import de.miangohar.overload.domain.logic.VolumeCalculator
import de.miangohar.overload.domain.model.MuscleGroup
import de.miangohar.overload.domain.model.SetEntry
import de.miangohar.overload.domain.model.TrainingWeek
import de.miangohar.overload.domain.model.WeeklyTotal
import de.miangohar.overload.domain.repository.GoalRepository
import de.miangohar.overload.domain.repository.SetEntryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.Clock
import java.time.LocalDate

/** One past week as shown in the history list. */
data class HistoryRow(
    val week: TrainingWeek,
    val totalSets: Double,
    val isDeload: Boolean,
)

data class HistoryUiState(
    val rows: List<HistoryRow> = emptyList(),
    val trend: List<WeeklyTotal> = emptyList(),
    val targetBand: ClosedFloatingPointRange<Double>? = null,
    val selectedMuscleGroup: MuscleGroup? = null,
    /** Every logged entry, unfiltered, for exporting the full log. */
    val entries: List<SetEntry> = emptyList(),
    val isLoading: Boolean = true,
)

/**
 * Lists every week that contains at least one logged entry, newest first,
 * and a fixed length trend for the chart above that list, filtered to one
 * muscle group or the total across all of them.
 */
class HistoryViewModel(
    setEntryRepository: SetEntryRepository,
    goalRepository: GoalRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ViewModel() {

    private val selectedMuscleGroup = MutableStateFlow<MuscleGroup?>(null)

    val uiState: StateFlow<HistoryUiState> = combine(
        setEntryRepository.observeAll(),
        goalRepository.observeGoal(),
        selectedMuscleGroup,
    ) { entries, goal, muscleGroup ->
        val totals = VolumeCalculator.weeklyTotals(entries)
        val currentWeek = TrainingWeek.containing(LocalDate.now(clock))
        HistoryUiState(
            rows = totals
                .mapIndexed { index, total ->
                    HistoryRow(
                        week = total.week,
                        totalSets = total.totalSets,
                        isDeload = DeloadAdvisor.isDeloadWeek(totals, index),
                    )
                }
                .reversed(),
            trend = VolumeCalculator.recentWeeklyTotals(
                entries,
                currentWeek,
                weekCount = TREND_WEEK_COUNT,
                muscleGroup = muscleGroup,
            ),
            targetBand = ProgressEvaluator.targetBand(goal, muscleGroup),
            selectedMuscleGroup = muscleGroup,
            entries = entries,
            isLoading = false,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = HistoryUiState(),
        )

    fun onTrendMuscleGroupSelected(muscleGroup: MuscleGroup?) {
        selectedMuscleGroup.update { muscleGroup }
    }

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5_000L
        const val TREND_WEEK_COUNT = 12

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as OverloadApplication
                HistoryViewModel(
                    setEntryRepository = application.container.setEntryRepository,
                    goalRepository = application.container.goalRepository,
                )
            }
        }
    }
}
