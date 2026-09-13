package de.miangohar.overload.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.miangohar.overload.domain.model.MuscleGroup
import de.miangohar.overload.domain.model.TrainingPhase
import de.miangohar.overload.domain.model.VolumeTarget

/**
 * The goal header. Exactly one row exists, identified by [SINGLETON_ID],
 * because the app tracks a single active goal at a time.
 */
@Entity(tableName = TrainingGoalEntity.TABLE_NAME)
data class TrainingGoalEntity(
    @PrimaryKey
    val id: Int = SINGLETON_ID,
    val phase: TrainingPhase,
    @ColumnInfo(name = "block_length_weeks")
    val blockLengthWeeks: Int,
) {
    companion object {
        const val TABLE_NAME = "training_goal"
        const val SINGLETON_ID = 1
    }
}

/** One weekly set range, one row per muscle group. */
@Entity(tableName = VolumeTargetEntity.TABLE_NAME)
data class VolumeTargetEntity(
    @PrimaryKey
    @ColumnInfo(name = "muscle_group")
    val muscleGroup: MuscleGroup,
    @ColumnInfo(name = "min_sets")
    val minSets: Int,
    @ColumnInfo(name = "max_sets")
    val maxSets: Int,
) {
    companion object {
        const val TABLE_NAME = "volume_targets"
    }
}

fun VolumeTargetEntity.toDomain(): VolumeTarget = VolumeTarget(
    muscleGroup = muscleGroup,
    minSets = minSets,
    maxSets = maxSets,
)

fun VolumeTarget.toEntity(): VolumeTargetEntity = VolumeTargetEntity(
    muscleGroup = muscleGroup,
    minSets = minSets,
    maxSets = maxSets,
)
