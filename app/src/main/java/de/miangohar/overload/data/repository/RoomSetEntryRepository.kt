package de.miangohar.overload.data.repository

import de.miangohar.overload.data.local.dao.SetEntryDao
import de.miangohar.overload.data.local.entity.toDomain
import de.miangohar.overload.data.local.entity.toEntity
import de.miangohar.overload.domain.model.SetEntry
import de.miangohar.overload.domain.model.TrainingWeek
import de.miangohar.overload.domain.repository.SetEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Room backed implementation of [SetEntryRepository]. */
class RoomSetEntryRepository(
    private val dao: SetEntryDao,
) : SetEntryRepository {

    override fun observeAll(): Flow<List<SetEntry>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeWeek(week: TrainingWeek): Flow<List<SetEntry>> =
        dao.observeBetween(week.startDate, week.endDate)
            .map { entities -> entities.map { it.toDomain() } }

    override suspend fun add(entry: SetEntry): Long =
        dao.insert(entry.toEntity().copy(id = SetEntry.NO_ID))

    override suspend fun update(entry: SetEntry) = dao.update(entry.toEntity())

    override suspend fun delete(id: Long) = dao.deleteById(id)
}
