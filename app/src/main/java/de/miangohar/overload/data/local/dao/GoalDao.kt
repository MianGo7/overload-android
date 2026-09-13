package de.miangohar.overload.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import de.miangohar.overload.data.local.entity.TrainingGoalEntity
import de.miangohar.overload.data.local.entity.VolumeTargetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Query("SELECT * FROM training_goal WHERE id = 1")
    fun observeGoal(): Flow<TrainingGoalEntity?>

    @Query("SELECT * FROM volume_targets ORDER BY muscle_group ASC")
    fun observeTargets(): Flow<List<VolumeTargetEntity>>

    @Upsert
    suspend fun upsertGoal(goal: TrainingGoalEntity)

    @Upsert
    suspend fun upsertTargets(targets: List<VolumeTargetEntity>)

    @Query("DELETE FROM volume_targets")
    suspend fun clearTargets()
}
