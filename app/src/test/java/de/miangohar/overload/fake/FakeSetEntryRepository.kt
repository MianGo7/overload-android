package de.miangohar.overload.fake

import de.miangohar.overload.domain.model.SetEntry
import de.miangohar.overload.domain.model.TrainingWeek
import de.miangohar.overload.domain.repository.SetEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In memory [SetEntryRepository] for view model tests, no Room involved. */
class FakeSetEntryRepository : SetEntryRepository {

    private val entries = MutableStateFlow<List<SetEntry>>(emptyList())

    /** All entries added so far, in insertion order. */
    val added: List<SetEntry> get() = entries.value

    override fun observeAll(): Flow<List<SetEntry>> = entries

    override fun observeWeek(week: TrainingWeek): Flow<List<SetEntry>> =
        entries.map { list -> list.filter { it.date in week } }

    override suspend fun add(entry: SetEntry): Long {
        val withId = entry.copy(id = entries.value.size + 1L)
        entries.value = entries.value + withId
        return withId.id
    }

    override suspend fun update(entry: SetEntry) {
        entries.value = entries.value.map { if (it.id == entry.id) entry else it }
    }

    override suspend fun delete(id: Long) {
        entries.value = entries.value.filterNot { it.id == id }
    }
}
