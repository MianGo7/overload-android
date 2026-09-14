package de.miangohar.overload.ui.goal

import de.miangohar.overload.R
import de.miangohar.overload.domain.model.MuscleGroup
import de.miangohar.overload.domain.model.TrainingGoal
import de.miangohar.overload.fake.FakeGoalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GoalViewModelTest {

    private val repository = FakeGoalRepository()
    private lateinit var viewModel: GoalViewModel

    // The view model must be created after the main dispatcher is set,
    // since its init block already launches a coroutine on viewModelScope.
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = GoalViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `a row with both fields empty is skipped`() {
        viewModel.onSaveRequested()

        assertTrue(viewModel.uiState.value.isSaved)
        assertNull(viewModel.uiState.value.errorRes)
        assertTrue(repository.savedGoal?.targets.orEmpty().isEmpty())
    }

    @Test
    fun `a row with only one field filled is rejected`() {
        viewModel.onMinChanged(MuscleGroup.CHEST, "10")

        viewModel.onSaveRequested()

        assertEquals(R.string.goal_error_number, viewModel.uiState.value.errorRes)
        assertTrue(viewModel.uiState.value.isSaved.not())
    }

    @Test
    fun `a minimum above the maximum is rejected`() {
        viewModel.onMinChanged(MuscleGroup.CHEST, "15")
        viewModel.onMaxChanged(MuscleGroup.CHEST, "5")

        viewModel.onSaveRequested()

        assertEquals(R.string.goal_error_range, viewModel.uiState.value.errorRes)
    }

    @Test
    fun `the block length is clamped to the permitted range`() {
        viewModel.onBlockLengthChanged(20)
        assertEquals(TrainingGoal.MAX_BLOCK_LENGTH_WEEKS, viewModel.uiState.value.blockLengthWeeks)

        viewModel.onBlockLengthChanged(1)
        assertEquals(TrainingGoal.MIN_BLOCK_LENGTH_WEEKS, viewModel.uiState.value.blockLengthWeeks)
    }

    @Test
    fun `a valid row is turned into a saved target`() {
        viewModel.onMinChanged(MuscleGroup.BACK, "10")
        viewModel.onMaxChanged(MuscleGroup.BACK, "16")

        viewModel.onSaveRequested()

        val target = repository.savedGoal?.targetFor(MuscleGroup.BACK)
        assertEquals(10, target?.minSets)
        assertEquals(16, target?.maxSets)
        assertTrue(viewModel.uiState.value.isSaved)
    }
}
