package de.miangohar.overload.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.miangohar.overload.domain.model.MuscleGroup
import de.miangohar.overload.domain.model.SetEntry
import java.time.LocalDate

/** Room representation of one logged block of sets. */
@Entity(tableName = SetEntryEntity.TABLE_NAME)
data class SetEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "date_epoch_day")
    val date: LocalDate,
    @ColumnInfo(name = "exercise_name")
    val exerciseName: String,
    @ColumnInfo(name = "primary_muscle")
    val primaryMuscle: MuscleGroup,
    @ColumnInfo(name = "secondary_muscles")
    val secondaryMuscles: Set<MuscleGroup>,
    val sets: Int,
    val reps: Int,
    @ColumnInfo(name = "weight_kg")
    val weightKg: Double,
    val rir: Int?,
) {
    companion object {
        const val TABLE_NAME = "set_entries"
    }
}

fun SetEntryEntity.toDomain(): SetEntry = SetEntry(
    id = id,
    date = date,
    exerciseName = exerciseName,
    primaryMuscle = primaryMuscle,
    secondaryMuscles = secondaryMuscles,
    sets = sets,
    reps = reps,
    weightKg = weightKg,
    rir = rir,
)

fun SetEntry.toEntity(): SetEntryEntity = SetEntryEntity(
    id = id,
    date = date,
    exerciseName = exerciseName,
    primaryMuscle = primaryMuscle,
    secondaryMuscles = secondaryMuscles,
    sets = sets,
    reps = reps,
    weightKg = weightKg,
    rir = rir,
)
