package dev.chsr.acuma.repository

import dev.chsr.acuma.dao.CategoryDao
import dev.chsr.acuma.dao.TransactionDao
import dev.chsr.acuma.entity.Category
import dev.chsr.acuma.entity.Transaction
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {

    fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAll()
    }

    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertAll(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.delete(transaction)
    }
}
