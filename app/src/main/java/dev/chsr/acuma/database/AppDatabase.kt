package dev.chsr.acuma.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.chsr.acuma.dao.CategoryDao
import dev.chsr.acuma.dao.TransactionDao
import dev.chsr.acuma.entity.Category
import dev.chsr.acuma.entity.Transaction

@Database(entities = [Category::class, Transaction::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, // важно!
                    AppDatabase::class.java,
                    "acuma-database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}