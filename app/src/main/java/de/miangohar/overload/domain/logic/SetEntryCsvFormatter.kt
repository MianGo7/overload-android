package de.miangohar.overload.domain.logic

import de.miangohar.overload.domain.model.SetEntry

/**
 * Turns logged entries into a comma separated values (CSV) file, RFC 4180
 * style. Touches no Android API, so it is covered by a plain JVM unit test;
 * writing the result to a file and sharing it are the activity's job, see
 * ADR-0016.
 */
object SetEntryCsvFormatter {

    private val header = listOf(
        "date", "exercise", "primary_muscle", "secondary_muscles", "sets", "reps", "weight_kg", "rir",
    )

    fun toCsv(entries: List<SetEntry>): String {
        val rows = entries.map { entry ->
            listOf(
                entry.date.toString(),
                entry.exerciseName,
                entry.primaryMuscle.name,
                entry.secondaryMuscles.joinToString(";") { it.name },
                entry.sets.toString(),
                entry.reps.toString(),
                entry.weightKg.toString(),
                entry.rir?.toString().orEmpty(),
            )
        }
        return (listOf(header) + rows).joinToString("\r\n") { row -> row.joinToString(",", transform = ::escape) }
    }

    /** Quotes a field and doubles internal quotes if it contains a comma, a quote or a newline. */
    private fun escape(field: String): String =
        if (field.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"${field.replace("\"", "\"\"")}\""
        } else {
            field
        }
}
