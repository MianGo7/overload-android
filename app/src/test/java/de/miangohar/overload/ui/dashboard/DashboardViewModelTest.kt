package de.miangohar.overload.ui.dashboard

import de.miangohar.overload.domain.model.TrainingWeek
import de.miangohar.overload.fake.FakeGoalRepository
import de.miangohar.overload.fake.FakeSetEntryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/** A [Clock] whose instant can be advanced from a test, to simulate time passing. */
private class MutableClock(
    private var instant: Instant,
    private val zone: ZoneId = ZoneOffset.UTC,
) : Clock() {
    fun advanceTo(newInstant: Instant) {
        instant = newInstant
    }

    override fun instant(): Instant = instant
    override fun getZone(): ZoneId = zone
    override fun withZone(zone: ZoneId): Clock = MutableClock(instant, zone)
}

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val setEntryRepository = FakeSetEntryRepository()
    private val goalRepository = FakeGoalRepository()
    private val clock = MutableClock(LocalDate.of(2026, 1, 4).atStartOfDay(ZoneOffset.UTC).toInstant())
    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = DashboardViewModel(setEntryRepository, goalRepository, clock)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // uiState is a stateIn(WhileSubscribed) flow, so it only runs its
    // combine() while something collects it, unlike GoalViewModel's or
    // LogEntryViewModel's plain MutableStateFlow; each test keeps a
    // collector running for that reason, rather than reading .value cold.

    @Test
    fun `resuming after the week rolls over moves the dashboard to the new week`() = runTest {
        val collector = launch(Dispatchers.Main) { viewModel.uiState.collect {} }
        val weekBefore = viewModel.uiState.value.week

        clock.advanceTo(LocalDate.of(2026, 1, 5).atStartOfDay(ZoneOffset.UTC).toInstant())
        viewModel.onResumed()

        val weekAfter = viewModel.uiState.value.week
        assertEquals(TrainingWeek.containing(LocalDate.of(2026, 1, 5)), weekAfter)
        assertNotEquals(weekBefore, weekAfter)
        collector.cancel()
    }

    @Test
    fun `resuming without the week changing leaves the dashboard on the same week`() = runTest {
        val collector = launch(Dispatchers.Main) { viewModel.uiState.collect {} }
        val weekBefore = viewModel.uiState.value.week

        viewModel.onResumed()

        assertEquals(weekBefore, viewModel.uiState.value.week)
        collector.cancel()
    }
}
