package de.miangohar.overload.ui.logentry

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.miangohar.overload.OverloadApplication
import de.miangohar.overload.R
import de.miangohar.overload.domain.model.MuscleGroup
import de.miangohar.overload.domain.model.SetEntry
import de.miangohar.overload.domain.repository.SetEntryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

data class LogEntryUiState(
    val date: LocalDate = LocalDate.now(),
    val exerciseName: String = "",
    val primaryMuscle: MuscleGroup = MuscleGroup.CHEST,
    val secondaryMuscles: Set<MuscleGroup> = emptySet(),
    val sets: String = "3",
    val reps: String = "10",
    val weightKg: String = "",
    val rir: String = "",
    val canShiftToLaterDay: Boolean = true,
    @StringRes val errorRes: Int? = null,
    val isSaved: Boolean = false,
)

/**
 * Collects one entry. The form keeps raw text and only converts it when the
 * user saves, so that a half typed number never crashes the screen.
 */
class LogEntryViewModel(
    private val setEntryRepository: SetEntryRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        LogEntryUiState(date = LocalDate.now(clock), canShiftToLaterDay = false),
    )
    val uiState: StateFlow<LogEntryUiState> = _uiState.asStateFlow()

    fun onExerciseNameChanged(value: String) = updateState { it.copy(exerciseName = value) }

    fun onSetsChanged(value: String) = updateState { it.copy(sets = value.filter(Char::isDigit)) }

    fun onRepsChanged(value: String) = updateState { it.copy(reps = value.filter(Char::isDigit)) }

    fun onWeightChanged(value: String) = updateState { it.copy(weightKg = value) }

    fun onRirChanged(value: String) = updateState { it.copy(rir = value.filter(Char::isDigit)) }

    /**
     * A workout cannot be logged before it happens, so a shift into the
     * future is clamped at today; the past stays unbounded since logging a
     * missed session retroactively is legitimate.
     */
    fun onDateShifted(days: Long) = updateState {
        val today = LocalDate.now(clock)
        val shifted = it.date.plusDays(days)
        it.copy(date = if (shifted > today) today else shifted)
    }

    fun onPrimaryMuscleSelected(muscleGroup: MuscleGroup) = updateState {
        it.copy(
            primaryMuscle = muscleGroup,
            secondaryMuscles = it.secondaryMuscles - muscleGroup,
        )
    }

    fun onSecondaryMuscleToggled(muscleGroup: MuscleGroup) = updateState { state ->
        if (muscleGroup == state.primaryMuscle) {
            state
        } else if (muscleGroup in state.secondaryMuscles) {
            state.copy(secondaryMuscles = state.secondaryMuscles - muscleGroup)
        } else {
            state.copy(secondaryMuscles = state.secondaryMuscles + muscleGroup)
        }
    }

    fun onSaveRequested() {
        val state = _uiState.value
        val sets = state.sets.toIntOrNull()
        val reps = state.reps.toIntOrNull()
        val weight = state.weightKg.replace(',', '.').toDoubleOrNull()

        if (state.exerciseName.isBlank() ||
            sets == null || sets <= 0 || sets > SetEntry.MAX_PLAUSIBLE_SETS ||
            reps == null || reps <= 0 || reps > SetEntry.MAX_PLAUSIBLE_REPS ||
            weight == null || weight < 0.0
        ) {
            _uiState.update { it.copy(errorRes = R.string.log_error_required) }
            return
        }

        val entry = SetEntry(
            date = state.date,
            exerciseName = state.exerciseName.trim(),
            primaryMuscle = state.primaryMuscle,
            secondaryMuscles = state.secondaryMuscles,
            sets = sets,
            reps = reps,
            weightKg = weight,
            rir = state.rir.toIntOrNull(),
        )

        viewModelScope.launch {
            setEntryRepository.add(entry)
            _uiState.update { it.copy(isSaved = true, errorRes = null) }
        }
    }

    /** Applies a change to the form state and clears any previous error. */
    private fun updateState(transform: (LogEntryUiState) -> LogEntryUiState) {
        _uiState.update { state ->
            val next = transform(state).copy(errorRes = null)
            next.copy(canShiftToLaterDay = next.date < LocalDate.now(clock))
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as OverloadApplication
                LogEntryViewModel(application.container.setEntryRepository)
            }
        }
    }
}
