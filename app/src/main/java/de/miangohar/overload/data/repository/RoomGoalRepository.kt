package de.miangohar.overload.data.repository

import androidx.room.withTransaction
import de.miangohar.overload.data.local.AppDatabase
import de.miangohar.overload.data.local.dao.GoalDao
import de.miangohar.overload.data.local.entity.TrainingGoalEntity
import de.miangohar.overload.data.local.entity.toDomain
import de.miangohar.overload.data.local.entity.toEntity
import de.miangohar.overload.domain.model.TrainingGoal
import de.miangohar.overload.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalTime

/**
 * Room backed implementation of [GoalRepository].
 *
 * The goal header and its targets live in two tables, so reads combine both
 * streams and writes run inside one transaction to keep them consistent.
 */
class RoomGoalRepository(
    private val database: AppDatabase,
    private val dao: GoalDao,
) : GoalRepository {

    override fun observeGoal(): Flow<TrainingGoal> =
        combine(dao.observeGoal(), dao.observeTargets()) { header, targets ->
            TrainingGoal(
                phase = header?.phase ?: TrainingGoal.EMPTY.phase,
                blockLengthWeeks = header?.blockLengthWeeks ?: TrainingGoal.EMPTY.blockLengthWeeks,
                targets = targets.map { it.toDomain() },
                reminderTime = header?.reminderTimeMinuteOfDay?.let { LocalTime.of(it / 60, it % 60) },
            )
        }

    override suspend fun saveGoal(goal: TrainingGoal) {
        database.withTransaction {
            dao.upsertGoal(
                TrainingGoalEntity(
                    phase = goal.phase,
                    blockLengthWeeks = goal.blockLengthWeeks,
                    reminderTimeMinuteOfDay = goal.reminderTime?.let { it.hour * 60 + it.minute },
                ),
            )
            dao.clearTargets()
            dao.upsertTargets(goal.targets.map { it.toEntity() })
        }
    }
}
