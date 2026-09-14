package de.miangohar.overload.ui.logentry

import de.miangohar.overload.R
import de.miangohar.overload.domain.model.MuscleGroup
import de.miangohar.overload.domain.model.SetEntry
import de.miangohar.overload.fake.FakeSetEntryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class LogEntryViewModelTest {

    private val repository = FakeSetEntryRepository()
    private lateinit var viewModel: LogEntryViewModel

    // The view model is created after the main dispatcher is set, so that
    // any future viewModelScope work in its init block is also covered.
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = LogEntryViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun fillValidEntry() {
        viewModel.onExerciseNameChanged("Bench Press")
        viewModel.onSetsChanged("3")
        viewModel.onRepsChanged("10")
        viewModel.onWeightChanged("60")
    }

    @Test
    fun `a blank exercise name is rejected`() {
        fillValidEntry()
        viewModel.onExerciseNameChanged("")

        viewModel.onSaveRequested()

        assertEquals(R.string.log_error_required, viewModel.uiState.value.errorRes)
        assertTrue(repository.added.isEmpty())
    }

    @Test
    fun `zero sets is rejected`() {
        fillValidEntry()
        viewModel.onSetsChanged("0")

        viewModel.onSaveRequested()

        assertEquals(R.string.log_error_required, viewModel.uiState.value.errorRes)
        assertTrue(repository.added.isEmpty())
    }

    @Test
    fun `zero reps is rejected`() {
        fillValidEntry()
        viewModel.onRepsChanged("0")

        viewModel.onSaveRequested()

        assertEquals(R.string.log_error_required, viewModel.uiState.value.errorRes)
        assertTrue(repository.added.isEmpty())
    }

    @Test
    fun `a missing weight is rejected`() {
        fillValidEntry()
        viewModel.onWeightChanged("")

        viewModel.onSaveRequested()

        assertEquals(R.string.log_error_required, viewModel.uiState.value.errorRes)
        assertTrue(repository.added.isEmpty())
    }

    @Test
    fun `a comma is accepted as a decimal separator`() {
        fillValidEntry()
        viewModel.onWeightChanged("62,5")

        viewModel.onSaveRequested()

        assertEquals(62.5, repository.added.single().weightKg, 0.0001)
    }

    @Test
    fun `a period is accepted as a decimal separator`() {
        fillValidEntry()
        viewModel.onWeightChanged("62.5")

        viewModel.onSaveRequested()

        assertEquals(62.5, repository.added.single().weightKg, 0.0001)
    }

    @Test
    fun `a value with both a thousands separator and a comma is rejected rather than corrupted`() {
        fillValidEntry()
        viewModel.onWeightChanged("1.234,5")

        viewModel.onSaveRequested()

        assertEquals(R.string.log_error_required, viewModel.uiState.value.errorRes)
        assertTrue(repository.added.isEmpty())
    }

    @Test
    fun `sets above the plausible ceiling are rejected`() {
        fillValidEntry()
        viewModel.onSetsChanged((SetEntry.MAX_PLAUSIBLE_SETS + 1).toString())

        viewModel.onSaveRequested()

        assertEquals(R.string.log_error_required, viewModel.uiState.value.errorRes)
        assertTrue(repository.added.isEmpty())
    }

    @Test
    fun `reps above the plausible ceiling are rejected`() {
        fillValidEntry()
        viewModel.onRepsChanged((SetEntry.MAX_PLAUSIBLE_REPS + 1).toString())

        viewModel.onSaveRequested()

        assertEquals(R.string.log_error_required, viewModel.uiState.value.errorRes)
        assertTrue(repository.added.isEmpty())
    }

    @Test
    fun `the date cannot be shifted into the future`() {
        val today = viewModel.uiState.value.date
        assertFalse(viewModel.uiState.value.canShiftToLaterDay)

        viewModel.onDateShifted(1L)

        assertEquals(today, viewModel.uiState.value.date)
    }

    @Test
    fun `the date can be shifted later again after being shifted earlier`() {
        viewModel.onDateShifted(-1L)
        assertTrue(viewModel.uiState.value.canShiftToLaterDay)

        viewModel.onDateShifted(1L)

        assertEquals(LocalDate.now(), viewModel.uiState.value.date)
        assertFalse(viewModel.uiState.value.canShiftToLaterDay)
    }

    @Test
    fun `selecting a muscle group as primary removes it from the secondary set`() {
        viewModel.onSecondaryMuscleToggled(MuscleGroup.TRICEPS)

        viewModel.onPrimaryMuscleSelected(MuscleGroup.TRICEPS)

        val state = viewModel.uiState.value
        assertEquals(MuscleGroup.TRICEPS, state.primaryMuscle)
        assertTrue(MuscleGroup.TRICEPS !in state.secondaryMuscles)
    }
}
