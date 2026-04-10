package net.dodiya.signalman.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleDao {
    @Query("SELECT * FROM rules ORDER BY priority DESC")
    fun getAllRules(): Flow<List<Rule>>

    @Query("SELECT * FROM rules WHERE id = :id")
    suspend fun getRule(id: Int): Rule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: Rule)

    @Update
    suspend fun updateRule(rule: Rule)

    @Delete
    suspend fun deleteRule(rule: Rule)

    @Query("DELETE FROM rules")
    suspend fun deleteAllRules()
}
