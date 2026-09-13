package de.miangohar.overload.domain.repository

import de.miangohar.overload.domain.model.SetEntry
import de.miangohar.overload.domain.model.TrainingWeek
import kotlinx.coroutines.flow.Flow

/**
 * Access to logged training entries.
 *
 * The interface lives in the domain layer and is implemented in the data
 * layer, so the domain and the UI never depend on Room.
 */
interface SetEntryRepository {

    /** All entries, newest first. */
    fun observeAll(): Flow<List<SetEntry>>

    /** Entries of a single training week, newest first. */
    fun observeWeek(week: TrainingWeek): Flow<List<SetEntry>>

    /** Inserts an entry and returns its generated identifier. */
    suspend fun add(entry: SetEntry): Long

    suspend fun update(entry: SetEntry)

    suspend fun delete(id: Long)
}
