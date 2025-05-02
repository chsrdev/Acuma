package dev.chsr.acuma.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.chsr.acuma.R
import dev.chsr.acuma.dao.CategoryDao
import dev.chsr.acuma.dao.TransactionDao
import dev.chsr.acuma.entity.Category
import dev.chsr.acuma.entity.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Category::class, Transaction::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
            CREATE TABLE transactions_temp (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                from_id INTEGER,
                to_id INTEGER,
                amount INTEGER NOT NULL,
                comment TEXT,
                date INTEGER NOT NULL
            )
            """.trimIndent()
                )


                db.execSQL("DROP TABLE transactions")
                db.execSQL("ALTER TABLE transactions_temp RENAME TO transactions")
            }
        }


        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val roomCallback = object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val reserveCategory = Category(
                                id = -1,
                                name = context.getString(R.string.reserve),
                                percent = 100,
                                balance = 0,
                                goal = null
                            )
                            getInstance(context).categoryDao().insertAll(reserveCategory)
                        }
                    }
                }

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "acuma-database"
                ).addMigrations(MIGRATION_1_2).addCallback(roomCallback).build()
                INSTANCE = instance
                instance
            }
        }
    }
}