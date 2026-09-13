package de.miangohar.overload.domain.logic

import de.miangohar.overload.domain.TestFixtures
import de.miangohar.overload.domain.model.MuscleGroup
import de.miangohar.overload.domain.model.VolumeStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressEvaluatorTest {

    private val delta = 0.0001

    @Test
    fun `volume below the lower bound is flagged as below target`() {
        val target = TestFixtures.target(MuscleGroup.CHEST, minSets = 10, maxSets = 16)

        assertEquals(VolumeStatus.BELOW_TARGET, ProgressEvaluator.statusFor(9.5, target))
    }

    @Test
    fun `both bounds of the range count as inside the target`() {
        val target = TestFixtures.target(MuscleGroup.CHEST, minSets = 10, maxSets = 16)

        assertEquals(VolumeStatus.IN_TARGET, ProgressEvaluator.statusFor(10.0, target))
        assertEquals(VolumeStatus.IN_TARGET, ProgressEvaluator.statusFor(16.0, target))
    }

    @Test
    fun `volume above the upper bound is flagged as above target`() {
        val target = TestFixtures.target(MuscleGroup.CHEST, minSets = 10, maxSets = 16)

        assertEquals(VolumeStatus.ABOVE_TARGET, ProgressEvaluator.statusFor(16.5, target))
    }

    @Test
    fun `a muscle group without a target is reported as untargeted`() {
        assertEquals(VolumeStatus.UNTARGETED, ProgressEvaluator.statusFor(12.0, null))
        assertEquals(0.0, ProgressEvaluator.completionRatio(12.0, null), delta)
    }

    @Test
    fun `the completion ratio is capped at one`() {
        val target = TestFixtures.target(MuscleGroup.CHEST, minSets = 10, maxSets = 16)

        assertEquals(0.5, ProgressEvaluator.completionRatio(5.0, target), delta)
        assertEquals(1.0, ProgressEvaluator.completionRatio(20.0, target), delta)
    }

    @Test
    fun `overall completion averages the targeted muscle groups only`() {
        val goal = TestFixtures.goal(
            TestFixtures.target(MuscleGroup.CHEST, minSets = 10, maxSets = 16),
            TestFixtures.target(MuscleGroup.BACK, minSets = 10, maxSets = 16),
        )
        val entries = listOf(
            TestFixtures.entry(primary = MuscleGroup.CHEST, sets = 10),
            TestFixtures.entry(primary = MuscleGroup.BACK, sets = 5),
            TestFixtures.entry(primary = MuscleGroup.CALVES, sets = 8),
        )

        val progress = ProgressEvaluator.evaluate(TestFixtures.WEEK, entries, goal)

        assertEquals(0.75, progress.overallCompletion, delta)
    }

    @Test
    fun `untracked work still shows up in the weekly breakdown`() {
        val goal = TestFixtures.goal(TestFixtures.target(MuscleGroup.CHEST, minSets = 10))
        val entries = listOf(TestFixtures.entry(primary = MuscleGroup.CALVES, sets = 8))

        val progress = ProgressEvaluator.evaluate(TestFixtures.WEEK, entries, goal)
        val calves = progress.muscleVolumes.single { it.muscleGroup == MuscleGroup.CALVES }

        assertEquals(8.0, calves.completedSets, delta)
        assertEquals(VolumeStatus.UNTARGETED, calves.status)
    }

    @Test
    fun `a target without logged volume is reported as below target`() {
        val goal = TestFixtures.goal(TestFixtures.target(MuscleGroup.HAMSTRINGS, minSets = 8))

        val progress = ProgressEvaluator.evaluate(TestFixtures.WEEK, emptyList(), goal)
        val hamstrings = progress.muscleVolumes.single()

        assertEquals(0.0, hamstrings.completedSets, delta)
        assertEquals(VolumeStatus.BELOW_TARGET, hamstrings.status)
        assertEquals(1, progress.musclesBelowTarget.size)
    }

    @Test
    fun `overall completion is zero when no target is configured`() {
        val entries = listOf(TestFixtures.entry(sets = 6))

        val progress = ProgressEvaluator.evaluate(TestFixtures.WEEK, entries, TestFixtures.goal())

        assertEquals(0.0, progress.overallCompletion, delta)
    }
}
