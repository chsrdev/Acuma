package dev.chsr.acuma.database

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.chsr.acuma.dao.CategoryDao
import dev.chsr.acuma.dao.TransactionDao
import dev.chsr.acuma.entity.Category
import dev.chsr.acuma.entity.Transaction

@Database(entities = [Category::class, Transaction::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
}