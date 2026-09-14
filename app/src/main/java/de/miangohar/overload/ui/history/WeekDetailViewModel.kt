package de.miangohar.overload.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.miangohar.overload.OverloadApplication
import de.miangohar.overload.domain.logic.ProgressEvaluator
import de.miangohar.overload.domain.model.SetEntry
import de.miangohar.overload.domain.model.TrainingWeek
import de.miangohar.overload.domain.model.WeeklyProgress
import de.miangohar.overload.domain.repository.GoalRepository
import de.miangohar.overload.domain.repository.SetEntryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class WeekDetailUiState(
    val week: TrainingWeek,
    val progress: WeeklyProgress?,
    val entries: List<SetEntry> = emptyList(),
)

/** Evaluates one selected past week against the current goal. */
class WeekDetailViewModel(
    private val setEntryRepository: SetEntryRepository,
    goalRepository: GoalRepository,
    private val week: TrainingWeek,
) : ViewModel() {

    val uiState: StateFlow<WeekDetailUiState> = combine(
        setEntryRepository.observeWeek(week),
        goalRepository.observeGoal(),
    ) { entries, goal ->
        WeekDetailUiState(
            week = week,
            progress = ProgressEvaluator.evaluate(week, entries, goal),
            entries = entries.sortedByDescending { it.date },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = WeekDetailUiState(week, null),
    )

    fun onDeleteRequested(id: Long) {
        viewModelScope.launch { setEntryRepository.delete(id) }
    }

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5_000L

        fun factory(weekStartEpochDay: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as OverloadApplication
                WeekDetailViewModel(
                    setEntryRepository = application.container.setEntryRepository,
                    goalRepository = application.container.goalRepository,
                    week = TrainingWeek.containing(LocalDate.ofEpochDay(weekStartEpochDay)),
                )
            }
        }
    }
}
