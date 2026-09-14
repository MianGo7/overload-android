package de.miangohar.overload.ui.history

import de.miangohar.overload.domain.TestFixtures
import de.miangohar.overload.fake.FakeGoalRepository
import de.miangohar.overload.fake.FakeSetEntryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeekDetailViewModelTest {

    private val setEntryRepository = FakeSetEntryRepository()
    private val goalRepository = FakeGoalRepository()
    private lateinit var viewModel: WeekDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = WeekDetailViewModel(setEntryRepository, goalRepository, TestFixtures.WEEK)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `deleting an entry removes it from the state and the repository`() = runTest {
        val collector = launch(Dispatchers.Main) { viewModel.uiState.collect {} }
        val id = runBlocking { setEntryRepository.add(TestFixtures.entry()) }
        val entryBefore = viewModel.uiState.value.entries.single()
        assertEquals(id, entryBefore.id)

        viewModel.onDeleteRequested(id)

        assertTrue(viewModel.uiState.value.entries.isEmpty())
        assertTrue(setEntryRepository.added.none { it.id == id })
        collector.cancel()
    }

    @Test
    fun `entries outside the week are not shown`() = runTest {
        val collector = launch(Dispatchers.Main) { viewModel.uiState.collect {} }

        runBlocking { setEntryRepository.add(TestFixtures.entry(date = TestFixtures.MONDAY.plusDays(30))) }

        assertTrue(viewModel.uiState.value.entries.isEmpty())
        collector.cancel()
    }
}
