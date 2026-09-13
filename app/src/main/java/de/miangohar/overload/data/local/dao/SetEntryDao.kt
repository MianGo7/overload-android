package de.miangohar.overload.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.miangohar.overload.data.local.entity.SetEntryEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface SetEntryDao {

    @Query("SELECT * FROM set_entries ORDER BY date_epoch_day DESC, id DESC")
    fun observeAll(): Flow<List<SetEntryEntity>>

    @Query(
        "SELECT * FROM set_entries " +
            "WHERE date_epoch_day BETWEEN :from AND :to " +
            "ORDER BY date_epoch_day DESC, id DESC",
    )
    fun observeBetween(from: LocalDate, to: LocalDate): Flow<List<SetEntryEntity>>

    @Insert
    suspend fun insert(entry: SetEntryEntity): Long

    @Update
    suspend fun update(entry: SetEntryEntity)

    @Query("DELETE FROM set_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}
