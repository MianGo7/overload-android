package de.miangohar.overload.data.local

import androidx.room.TypeConverter
import de.miangohar.overload.domain.model.MuscleGroup
import de.miangohar.overload.domain.model.TrainingPhase
import java.time.LocalDate

/**
 * Converts the few domain types that SQLite cannot store directly.
 *
 * Dates are stored as epoch days rather than as text so that they can be
 * compared and ordered in SQL. Enums are stored by name, which keeps the
 * database readable and survives reordering of the enum constants.
 */
class Converters {

    @TypeConverter
    fun localDateToEpochDay(value: LocalDate?): Long? = value?.toEpochDay()

    @TypeConverter
    fun epochDayToLocalDate(value: Long?): LocalDate? = value?.let(LocalDate::ofEpochDay)

    @TypeConverter
    fun muscleGroupToName(value: MuscleGroup?): String? = value?.name

    @TypeConverter
    fun nameToMuscleGroup(value: String?): MuscleGroup? = value?.let(MuscleGroup::valueOf)

    @TypeConverter
    fun trainingPhaseToName(value: TrainingPhase?): String? = value?.name

    @TypeConverter
    fun nameToTrainingPhase(value: String?): TrainingPhase? = value?.let(TrainingPhase::valueOf)

    @TypeConverter
    fun muscleGroupsToString(value: Set<MuscleGroup>?): String =
        value.orEmpty().joinToString(SEPARATOR) { it.name }

    @TypeConverter
    fun stringToMuscleGroups(value: String?): Set<MuscleGroup> =
        value.orEmpty()
            .split(SEPARATOR)
            .filter { it.isNotBlank() }
            .map(MuscleGroup::valueOf)
            .toSet()

    private companion object {
        const val SEPARATOR = ","
    }
}
