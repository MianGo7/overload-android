package de.miangohar.overload.domain.logic

import de.miangohar.overload.domain.TestFixtures
import de.miangohar.overload.domain.model.MuscleGroup
import org.junit.Assert.assertEquals
import org.junit.Test

class SetEntryCsvFormatterTest {

    @Test
    fun `an empty list produces only the header row`() {
        val csv = SetEntryCsvFormatter.toCsv(emptyList())

        assertEquals("date,exercise,primary_muscle,secondary_muscles,sets,reps,weight_kg,rir", csv)
    }

    @Test
    fun `one entry becomes one row after the header`() {
        val entry = TestFixtures.entry(
            date = TestFixtures.MONDAY,
            exerciseName = "Bench Press",
            primary = MuscleGroup.CHEST,
            secondary = emptySet(),
            sets = 3,
            reps = 10,
            weightKg = 60.0,
            rir = 2,
        )

        val csv = SetEntryCsvFormatter.toCsv(listOf(entry))
        val lines = csv.split("\r\n")

        assertEquals(2, lines.size)
        assertEquals("2026-01-05,Bench Press,CHEST,,3,10,60.0,2", lines[1])
    }

    @Test
    fun `secondary muscles are joined with a semicolon`() {
        val entry = TestFixtures.entry(secondary = setOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS_SIDE))

        val csv = SetEntryCsvFormatter.toCsv(listOf(entry))

        assertEquals(
            "TRICEPS;SHOULDERS_SIDE",
            csv.split("\r\n")[1].split(",")[3],
        )
    }

    @Test
    fun `a missing reps in reserve is an empty field`() {
        val entry = TestFixtures.entry(rir = null)

        val csv = SetEntryCsvFormatter.toCsv(listOf(entry))

        assertEquals("", csv.split("\r\n")[1].split(",").last())
    }

    @Test
    fun `an exercise name containing a comma is quoted so the field count stays correct`() {
        val entry = TestFixtures.entry(exerciseName = "Row, Barbell")

        val row = SetEntryCsvFormatter.toCsv(listOf(entry)).split("\r\n")[1]

        assertEquals("\"Row, Barbell\"", exerciseField(row))
    }

    @Test
    fun `an exercise name containing a quote doubles the quote and wraps the field`() {
        val entry = TestFixtures.entry(exerciseName = "36\" Box Jump")

        val row = SetEntryCsvFormatter.toCsv(listOf(entry)).split("\r\n")[1]

        assertEquals("\"36\"\" Box Jump\"", exerciseField(row))
    }

    /** The second field of a data row, still quoted if the formatter quoted it. */
    private fun exerciseField(row: String): String =
        row.substringAfter(",").substringBeforeLast(",CHEST")
}
