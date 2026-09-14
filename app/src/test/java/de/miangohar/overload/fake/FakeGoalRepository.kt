package de.miangohar.overload.fake

import de.miangohar.overload.domain.model.TrainingGoal
import de.miangohar.overload.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In memory [GoalRepository] for view model tests, no Room involved. */
class FakeGoalRepository(initialGoal: TrainingGoal = TrainingGoal.EMPTY) : GoalRepository {

    private val goal = MutableStateFlow(initialGoal)

    /** The last goal passed to [saveGoal], null if none was saved yet. */
    var savedGoal: TrainingGoal? = null
        private set

    override fun observeGoal(): Flow<TrainingGoal> = goal

    override suspend fun saveGoal(goal: TrainingGoal) {
        this.goal.value = goal
        savedGoal = goal
    }
}
