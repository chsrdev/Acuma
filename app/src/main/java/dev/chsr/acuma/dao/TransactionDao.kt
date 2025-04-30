package dev.chsr.acuma.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import dev.chsr.acuma.entity.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions")
    fun getAll(): Flow<List<Transaction>>

    @Insert
    suspend fun insertAll(vararg transactions: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)
}