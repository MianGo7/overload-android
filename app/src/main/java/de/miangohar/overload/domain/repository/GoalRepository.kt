package de.miangohar.overload.domain.repository

import de.miangohar.overload.domain.model.TrainingGoal
import kotlinx.coroutines.flow.Flow

/** Access to the single training goal the user configures. */
interface GoalRepository {

    /** Emits [TrainingGoal.EMPTY] until the user has saved a goal. */
    fun observeGoal(): Flow<TrainingGoal>

    suspend fun saveGoal(goal: TrainingGoal)
}
