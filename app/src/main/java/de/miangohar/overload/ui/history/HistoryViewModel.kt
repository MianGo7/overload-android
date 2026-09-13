package de.miangohar.overload.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.miangohar.overload.OverloadApplication
import de.miangohar.overload.domain.logic.DeloadAdvisor
import de.miangohar.overload.domain.logic.VolumeCalculator
import de.miangohar.overload.domain.model.TrainingWeek
import de.miangohar.overload.domain.repository.SetEntryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** One past week as shown in the history list. */
data class HistoryRow(
    val week: TrainingWeek,
    val totalSets: Double,
    val isDeload: Boolean,
)

data class HistoryUiState(
    val rows: List<HistoryRow> = emptyList(),
    val isLoading: Boolean = true,
)

/** Lists every week that contains at least one logged entry, newest first. */
class HistoryViewModel(
    setEntryRepository: SetEntryRepository,
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = setEntryRepository.observeAll()
        .map { entries ->
            val totals = VolumeCalculator.weeklyTotals(entries)
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
                isLoading = false,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = HistoryUiState(),
        )

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5_000L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as OverloadApplication
                HistoryViewModel(application.container.setEntryRepository)
            }
        }
    }
}
