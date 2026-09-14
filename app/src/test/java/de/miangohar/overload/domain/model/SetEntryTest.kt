package de.miangohar.overload.domain.model

import de.miangohar.overload.domain.TestFixtures
import org.junit.Test

class SetEntryTest {

    @Test(expected = IllegalArgumentException::class)
    fun `a blank exercise name is rejected`() {
        TestFixtures.entry(exerciseName = " ")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `zero sets is rejected`() {
        TestFixtures.entry(sets = 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `sets above the plausible ceiling are rejected`() {
        TestFixtures.entry(sets = SetEntry.MAX_PLAUSIBLE_SETS + 1)
    }

    @Test
    fun `sets at the plausible ceiling are accepted`() {
        TestFixtures.entry(sets = SetEntry.MAX_PLAUSIBLE_SETS)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `zero reps is rejected`() {
        TestFixtures.entry(reps = 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `reps above the plausible ceiling are rejected`() {
        TestFixtures.entry(reps = SetEntry.MAX_PLAUSIBLE_REPS + 1)
    }

    @Test
    fun `reps at the plausible ceiling are accepted`() {
        TestFixtures.entry(reps = SetEntry.MAX_PLAUSIBLE_REPS)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `negative weight is rejected`() {
        TestFixtures.entry(weightKg = -1.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `negative reps in reserve is rejected`() {
        TestFixtures.entry(rir = -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `the primary muscle cannot also be listed as a secondary muscle`() {
        TestFixtures.entry(primary = MuscleGroup.CHEST, secondary = setOf(MuscleGroup.CHEST))
    }
}
