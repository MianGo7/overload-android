package de.miangohar.overload.ui.goal

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.miangohar.overload.OverloadApplication
import de.miangohar.overload.R
import de.miangohar.overload.domain.logic.DefaultTargets
import de.miangohar.overload.domain.model.MuscleGroup
import de.miangohar.overload.domain.model.TrainingGoal
import de.miangohar.overload.domain.model.TrainingPhase
import de.miangohar.overload.domain.model.VolumeTarget
import de.miangohar.overload.domain.repository.GoalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** One editable row of the goal form. Empty fields mean the group is not tracked. */
data class TargetRow(
    val muscleGroup: MuscleGroup,
    val minSets: String,
    val maxSets: String,
)

data class GoalUiState(
    val phase: TrainingPhase = TrainingPhase.BULK,
    val blockLengthWeeks: Int = TrainingGoal.DEFAULT_BLOCK_LENGTH_WEEKS,
    val rows: List<TargetRow> = MuscleGroup.entries.map { TargetRow(it, "", "") },
    @StringRes val errorRes: Int? = null,
    val isSaved: Boolean = false,
)

/** Edits the single training goal. Input stays as text until it is validated. */
class GoalViewModel(
    private val goalRepository: GoalRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalUiState())
    val uiState: StateFlow<GoalUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val goal = goalRepository.observeGoal().first()
            _uiState.update { state ->
                state.copy(
                    phase = goal.phase,
                    blockLengthWeeks = goal.blockLengthWeeks,
                    rows = rowsOf(goal.targets),
                )
            }
        }
    }

    fun onPhaseSelected(phase: TrainingPhase) {
        _uiState.update { it.copy(phase = phase, errorRes = null) }
    }

    fun onBlockLengthChanged(weeks: Int) {
        val clamped = weeks.coerceIn(
            TrainingGoal.MIN_BLOCK_LENGTH_WEEKS,
            TrainingGoal.MAX_BLOCK_LENGTH_WEEKS,
        )
        _uiState.update { it.copy(blockLengthWeeks = clamped, errorRes = null) }
    }

    fun onMinChanged(muscleGroup: MuscleGroup, value: String) {
        updateRow(muscleGroup) { it.copy(minSets = value.filter(Char::isDigit)) }
    }

    fun onMaxChanged(muscleGroup: MuscleGroup, value: String) {
        updateRow(muscleGroup) { it.copy(maxSets = value.filter(Char::isDigit)) }
    }

    /** Fills the form with conventional starting ranges for the selected phase. */
    fun onPrefillRequested() {
        _uiState.update { state ->
            state.copy(rows = rowsOf(DefaultTargets.forPhase(state.phase)), errorRes = null)
        }
    }

    fun onSaveRequested() {
        val state = _uiState.value
        val targets = mutableListOf<VolumeTarget>()

        for (row in state.rows) {
            val hasMin = row.minSets.isNotBlank()
            val hasMax = row.maxSets.isNotBlank()
            if (!hasMin && !hasMax) continue
            if (hasMin != hasMax) {
                _uiState.update { it.copy(errorRes = R.string.goal_error_number) }
                return
            }
            val min = row.minSets.toIntOrNull()
            val max = row.maxSets.toIntOrNull()
            if (min == null || max == null) {
                _uiState.update { it.copy(errorRes = R.string.goal_error_number) }
                return
            }
            if (min > max) {
                _uiState.update { it.copy(errorRes = R.string.goal_error_range) }
                return
            }
            targets += VolumeTarget(row.muscleGroup, min, max)
        }

        viewModelScope.launch {
            goalRepository.saveGoal(
                TrainingGoal(
                    phase = state.phase,
                    blockLengthWeeks = state.blockLengthWeeks,
                    targets = targets,
                ),
            )
            _uiState.update { it.copy(isSaved = true, errorRes = null) }
        }
    }

    private fun updateRow(muscleGroup: MuscleGroup, transform: (TargetRow) -> TargetRow) {
        _uiState.update { state ->
            state.copy(
                rows = state.rows.map { row ->
                    if (row.muscleGroup == muscleGroup) transform(row) else row
                },
                errorRes = null,
            )
        }
    }

    private fun rowsOf(targets: List<VolumeTarget>): List<TargetRow> =
        MuscleGroup.entries.map { muscleGroup ->
            val target = targets.firstOrNull { it.muscleGroup == muscleGroup }
            TargetRow(
                muscleGroup = muscleGroup,
                minSets = target?.minSets?.toString().orEmpty(),
                maxSets = target?.maxSets?.toString().orEmpty(),
            )
        }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as OverloadApplication
                GoalViewModel(application.container.goalRepository)
            }
        }
    }
}
